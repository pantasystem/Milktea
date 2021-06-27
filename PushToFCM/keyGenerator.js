const crypto = require('crypto');
const util = require('util');
const urlsafeBase64 = require('urlsafe-base64');

const keyCurve = crypto.createECDH('prime256v1');
keyCurve.generateKeys();

console.log("public:", urlsafeBase64.encode(keyCurve.getPublicKey()));
console.log("private:", urlsafeBase64.encode(keyCurve.getPrivateKey()));
console.log("auth:", urlsafeBase64.encode(crypto.randomBytes(16)));