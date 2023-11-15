//
// WebPushをdecryptするヤツ(on Node.js)
//
// **参考**
// https://tools.ietf.org/html/rfc8188
// https://tools.ietf.org/html/rfc8291
// https://tools.ietf.org/html/rfc8291#appendix-A
// https://gist.github.com/tateisu/685eab242549d9c9ffc85020f09a4b71
// ↑一部 @tateisu氏のコードを参考にしています
// (アドバイスありがとうございました！→ https://mastodon.juggler.jp/@tateisu/104098620591598243)
// こちらのコードは@YuigaWadaさんのコードを一部改変して利用しています。
// https://github.com/YuigaWada/MissCat/blob/develop/ApiServer/api.js
const util = require("util");
const crypto = require("crypto");
const encoding = require('encoding-japanese');

function decodeBase64(src) {
  return Buffer.from(src, "base64");
}

function sha256(key, data) {
  return crypto.createHmac("sha256", key).update(data).digest();
}

function log(verbose, label, text) {
  if (!verbose) { return; }
  console.log(label, text);
}

// 通知を受け取る側で生成したキーを渡す
exports.buildReciverKey = function (public, private, authSecret) {
  this.public = decodeBase64(public);
  this.private = decodeBase64(private);
  this.authSecret = decodeBase64(authSecret);
  return this;
};

// WebPushで流れてきた通知をdecrypt
exports.decrypt = function (body64, receiverKey, verbose) {
  let body = decodeBase64(body64);

  // bodyを分解してsalt, keyid, 暗号化されたcontentsを取り出す
  // bodyの構造は以下の通り↓
  /*
  https://tools.ietf.org/id/draft-ietf-httpbis-encryption-encoding-09.xml#header
	+-----------+--------+-----------+---------------+
	| salt (16) | rs (4) | idlen (1) | keyid (idlen) |
	+-----------+--------+-----------+---------------+
	*/

  const salt = body.slice(0, 16);
  const rs = body.slice(16, 16 + 4);

  const idlen_hex = body.slice(16 + 4, 16 + 4 + 1).toString("hex");
  const idlen = parseInt(idlen_hex, 16); // keyidの長さ
  const keyid = body.slice(16 + 4 + 1, 16 + 4 + 1 + idlen);

  const content = body.slice(16 + 4 + 1 + idlen, body.length);

  // For Verbose Mode
  log(verbose, "salt", salt.toString("base64"));
  log(verbose, "rs", rs.toString("hex"));
  log(verbose, "idlen_hex", idlen_hex);
  log(verbose, "idlen", idlen);
  log(verbose, "keyid", keyid.toString("base64"));

  return decryptContent(content, receiverKey, keyid, verbose);
};

function decryptContent(content, receiverKey, keyid ,verbose) {
  let auth_secret = receiverKey.authSecret;
  let receiver_public = receiverKey.public;
  let receiver_private = receiverKey.private;

  const sender_public = decodeBase64(keyid.toString("base64"));

  // 共有秘密鍵を生成(ECDH)
  let receiver_curve = crypto.createECDH("prime256v1");
  receiver_curve.setPrivateKey(receiver_private);
  const sharedSecret = receiver_curve.computeSecret(keyid);

  /*
	  # HKDF-Extract(salt=auth_secret, IKM=ecdh_secret)
	  PRK_key = HMAC-SHA-256(auth_secret, ecdh_secret)
	  # HKDF-Expand(PRK_key, key_info, L_key=32)
	  key_info = "WebPush: info" || 0x00 || ua_public || as_public
	  IKM = HMAC-SHA-256(PRK_key, key_info || 0x01)
	  ## HKDF calculations from RFC 8188
	  # HKDF-Extract(salt, IKM)
	  PRK = HMAC-SHA-256(salt, IKM)
	  # HKDF-Expand(PRK, cek_info, L_cek=16)
	  cek_info = "Content-Encoding: aes128gcm" || 0x00
	  CEK = HMAC-SHA-256(PRK, cek_info || 0x01)[0..15]
	  # HKDF-Expand(PRK, nonce_info, L_nonce=12)
	  nonce_info = "Content-Encoding: nonce" || 0x00
	  NONCE = HMAC-SHA-256(PRK, nonce_info || 0x01)[0..11]
	*/

  // key
  const prk_key = sha256(auth_secret, sharedSecret);
  const keyInfo = Buffer.concat([
    Buffer.from("WebPush: info\0"),
    receiver_public,
    sender_public,
    Buffer.from("\1")
  ]);
  const ikm = sha256(prk_key, keyInfo);

  // prk
  // https://tools.ietf.org/id/draft-ietf-httpbis-encryption-encoding-09.xml#derivation
  const prk = sha256(salt, ikm);
  log(verbose, "prk", prk.toString("base64"));

  // cek
  // https://tools.ietf.org/id/draft-ietf-httpbis-encryption-encoding-09.xml#derivation
  const cekInfo = Buffer.from("Content-Encoding: aes128gcm\0\1");
  const cek = sha256(prk, cekInfo).slice(0, 16);
  log(verbose, "cek", cek.toString("base64"));

  // initialization vector
  // https://tools.ietf.org/id/draft-ietf-httpbis-encryption-encoding-09.xml#nonce
  const nonceInfo = Buffer.from("Content-Encoding: nonce\0\1");
  const nonce = sha256(prk, nonceInfo).slice(0, 12);
  const iv = nonce;
  log(verbose, "nonce:", nonce.toString("base64"));

  // aes-128-gcm
  const decipher = crypto.createDecipheriv("aes-128-gcm", cek, iv);
  let result = decipher.update(content);
  log(verbose, 'type:', typeof(result));
  log(verbose, '文字コード:', encoding.detect(result));
  log(verbose, "decrypted: ", result.toString("utf8"));

  // remove padding and GCM auth tag
  while (result.slice(result.length-1,result.length) != "}") { // jsonの末端が見えるまで一文字ずつ消していく
    result = result.slice(0,result.length-1);
  }

  log(verbose, "shaped:", result.toString("utf8"));
  return result.toString("utf8");
}

exports.decryptContent = decryptContent;