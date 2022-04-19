# Milktea

<img src="https://github.com/Kinoshita0623/MisskeyAndroidClient/blob/master/app/src/main/ic_launcher-web.png?raw=true" width="100px">
<br>
Why don't you have Milktea with Misskey?<br>
Misskey Android client app.<br>

## Introduction
Milktea is Android client app specialized for [Misskey](https://github.com/misskey-dev/misskey).<br>

## Purpose
Milktea was developed to achieve these purposes below.
- Provide as like Android UI
- Support as many Misskey features as possible
- Comfortable touch even if you have migrated from other apps 
- Unique features to make Milktea easier to use
- Getting more people to use Misskey
- Develop Milktea on an ongoing basis

## Features
### Timeline
Milktea can display Timeline from Misskey in real time.<br>

### Timeline Tab function
よく表示するタイムラインを上部のタブに固定＆並び替えをすることができます。<br>
タブ機能は以下のタイムラインの項目を固定することができます。
- Global Timeline
- Social Timeline
- Local Timeline
- Home Timeline
- List Timeline
- List of user's Notes
- Search Results
- Antenna Timeline
- Gallery
- List of threads
- Favorite
- Notification
### Posting Note
You can create Note from Milktea.<br>
ファイルのアップロードが非同期で行われるため、<br>
投稿時にファイルアップロードを待つ必要がありません。<br>

### Reaction Picker
The function of make a reaction for Note.<br>
Reaction picker is categorized by custom emojis and emojis on the tabs.<br>
- Pinned setting
- Frequently used Reaction
- Several categories

### Save drafts of Note
Milktea's unique feature.<br>
You can save a draft of Note while making it.
### Drive
You can see your own files in Misskey Drive.

### Overwriting display name
Misskeyでは表記上のニックネームと、<br>
実際にユーザー間の会話で用いられる呼び名が異なることがありました。<br>
表示名と呼び名が異なるのは非常にややこしかったので、<br>
表面的にニックネームを上書き＆表示する機能を実装しました。<br>


## Installation
Download from [Google Play Store](https://play.google.com/store/apps/details?id=jp.panta.misskeyandroidclient) and install into your device.

Create your account on the instance you wish to use.<br>
[About Misskey](https://misskey-hub.net/en/docs/misskey.html)
[List of Instances](https://misskey-hub.net/en/instances.html)

After installation is complete, launch Milktea app.
認可画面が表示されるので、利用しようとしているインスタンスのURLを入力します。<br>
For example, when you want to use misskey.io, typing "misskey.io" .

You can freely change "app name".<br>
"app name" maybe displaied with "via" depending on the version of the instance.<br>

準備ができれば AUTHENTICATION (認証)を押します。<br>
<img src="https://user-images.githubusercontent.com/38454985/81928170-d03c8080-961f-11ea-8acc-b1d752d72de7.png" width="320px">

認証画面がブラウザに表示されるので、問題がなければ許可(Accept)を押します。<br>
もし、リダイレクトしない場合は戻るボタンを押して、「私は許可をしました」を押してください。
<img src="https://user-images.githubusercontent.com/38454985/81928454-3cb77f80-9620-11ea-839b-ea28962a0a92.png" width="320px">

成功すればMilkteaにリダイレクトするので[CONTINUE]を押し完了です。<br>
<img src="https://user-images.githubusercontent.com/38454985/81928572-6c668780-9620-11ea-800a-bbb03721ce8e.png" width="320px">



## Build

"git clone" this project and making local.properties<br>
```
touch local.properties
```
local.propertiesには<br>
以下のような属性を追加してプッシュ通知の中継鯖についての設定をします。<br>
プッシュ通知中継サーバについて<br>
https://github.com/pantasystem/MisskeyAndroidClient/blob/develop/PushToFCM/README.md<br>

push_to_fcm.server_base_urlにはプッシュ通知サーバのベースURLを設定します。<br>
push_to_fcm.public_keyにはPushToFCMで生成したpublicを設定します。<br>
push_to_fcm.authにはPushToFCMで生成したauthを設定します。

```
push_to_fcm.server_base_url=https://hogehogehoge-pus
push_to_fcm.public_key=中継鯖（PushToFCM）に設定したpublic_keyを設定します
push_to_fcm.auth=中継鯖に設定したauth_secret.txtを設定します
```
Building in Android SDK or AndroidStudio.
