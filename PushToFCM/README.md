# プッシュ通知中継サーバ
これはプッシュ通知を受信するための中継サーバプログラムです。

## 説明
Misskeyからプッシュ通知を受信するには
中継サーバを構築して、そこからFirebase Cloud Massaging経由で通知を送信します。

## 使用方法
### secretとkeyの生成
初めにkeyGenerator.jsをnode.jsで実行してプッシュ通知を使用するための鍵を取得します。<br>

```
node keyGenerator.js 
public: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
private: yyyyyyyyyyyyyyyyyyyyyyyyyyyyy
auth: vvvvvvvvvvvvvv
```

keyディレクトリに以下のファイルを作成します。<br>
auth_secret.txt<br>
private_key.txt<br>
public_key.txt<br>
```
touch ./key/auth_secret.txt
touch ./key/private_key.txt
touch ./key/public_key.txt
```

それぞれのファイルにkeyGenerator.jsで生成した値を設定します。<br>
publicを./key/public_key.txt<br>
privateを./key/private_key.txt<br>
authを./key/auth_secret.txt<br>
```
echo xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx > ./key/public_key.txt
echo yyyyyyyyyyyyyyyyyyyyyyyyyyyyy > ./key/private_key.txt
echo vvvvvvvvvvvvvv > ./key/auth_secret.txt
```

### Firebase adminの設定
Firebase Cloud Messagingに接続するために<br>
サーバーにFirebase Adminの設定をする必要があります。
https://firebase.google.cn/docs/admin/setup?hl=ja<br>
```
export GOOGLE_APPLICATION_CREDENTIALS=firebase-adminのjsonファイル.json
```

index.jsを起動します。<br>
