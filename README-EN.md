# Milktea

<img src="https://github.com/Kinoshita0623/MisskeyAndroidClient/blob/master/app/src/main/ic_launcher-web.png?raw=true" width="100px">
<br>
Would you like Milktea with Misskey?<br>
Misskey Android client app<br>

## Introduction
Milktea is Android client app specialized for [Misskey](https://github.com/misskey-dev/misskey).<br>

## Purpose
Milktea was developed to achieve these purposes below.
- Provide as like Android UI
- Support as many Misskey features as possible
- Comfortable touch even if you have migrated from other social network apps 
- Unique features to make Milktea easier to use
- Get more people to use Misskey
- Develop Milktea on an ongoing basis

## Feature
### Timeline
Milktea can display Timeline from Misskey instance in real time.<br>

### Timeline Tab function
You can fix and rearrange the most frequently viewed timelines at the top of tabs.<br>
The tab function can be used to fix the timelines below.
- Global Timeline
- Social Timeline
- Local Timeline
- Home Timeline
- List Timeline
- List of user's Notes
- Search Result
- Antenna Timeline
- Gallery
- List of threads
- Favorite
- Notification
### Posting Note
You can create and post Notes from Milktea.<br>
There is no need to wait for a file upload when posting Notes because its upload is done asynchronously.<br>

### Reaction Picker
The function of making a reaction for Notes.<br>
Reaction picker is categorized by custom emojis and emojis on the tabs.<br>
- Pinned setting
- Frequently used emojis
- Several (custom)emoji categories

### Save drafts of Notes
This is one of the unique feature for Milktea.<br>
You can save a draft of Notes while creating it.
### Drive
You can see your own files in Misskey Drive.

### Overwrite display name
 In Misskey, there was a case that the Name displayed on the screen was different from the nickname which used in the conversation between Misskey users. It was very complicated to have a difference of Name and the nickname. So, I implemented a function to overwrite the nickname and only display it on the Milktea.<br>


## Installation
Download from [Google Play Store](https://play.google.com/store/apps/details?id=jp.panta.misskeyandroidclient) and install into your device.

Create your account on the instance you wish to use.<br>
[About Misskey](https://misskey-hub.net/en/docs/misskey.html)
[List of Instances](https://misskey-hub.net/en/instances.html)

Launch the app after its installation is complete.
When "Authentication" screen appears, type Misskey instance URL you're trying to use.<br>
For example, when you want to use misskey.io, type "misskey.io" .

You can freely change "App name".<br>
"App name" maybe displaied with "via" on the instance depending on a version of it.<br>

Press AUTHENTICATION when you're ready.<br>
<img src="https://user-images.githubusercontent.com/38454985/81928170-d03c8080-961f-11ea-8acc-b1d752d72de7.png" width="320px">

The authentication screen will appear in your default browser. If there is no problem, click "Accept".<br>
If you're not redirected to the app, press the "Back" button and press "I have given permission".
<img src="https://user-images.githubusercontent.com/38454985/81928454-3cb77f80-9620-11ea-839b-ea28962a0a92.png" width="320px">

If successful, you will be redirected to Milktea and press "CONTINUE" to complete.<br>
<img src="https://user-images.githubusercontent.com/38454985/81928572-6c668780-9620-11ea-800a-bbb03721ce8e.png" width="320px">

## Build

"git clone" this project and make a file "local.properties".<br>
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
