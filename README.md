# Milktea

<img src="https://github.com/Kinoshita0623/MisskeyAndroidClient/blob/master/app/src/main/ic_launcher-web.png?raw=true" width="100px">
<br>
MisskeyにMilkteaはいかが？<br>
MisskeyのAndroidクライアント<br>

## 説明
Milkteaは[Misskey](https://github.com/misskey-dev/misskey)のためのAndroidクライアントアプリケーションです。<br>

## 目標
Milkteaでは以下のことを達成することを目標とし開発をしました。
- AndroidらしいUIで提供すること
- Misskeyの機能をできるだけ多くサポートすること
- 競合サービスから移住してきても違和感なく触れるUIであること
- 独自機能を追加してより使いやすくすること
- Misskeyをより多くの人に使ってもらうこと
- 継続的な開発ができること

## 機能
### タイムライン
Misskeyから流れてきたタイムラインを、<br>
リアルタイムで表示することができます。<br>

### タイムラインタブ機能
よく表示するタイムラインを上部のタブに固定＆並び替えをすることができます。<br>
タブ機能は以下のタイムラインの項目を固定することができます。
- グローバルタイムライン
- ソーシャルタイムライン
- ローカルタイムライン
- ホームタイムライン
- ユーザーリストタイムライン
- ユーザーの投稿一覧
- 検索結果
- アンテナタイムライン
- ギャラリー
- スレッド一覧
- お気に入り
- 通知
### ノート投稿
Milkteaから投稿を作成することができます。<br>
ファイルのアップロードが非同期で行われるため、<br>
投稿時にファイルアップロードを待つ必要がありません。<br>

### リアクションピッカー
ノートにリアクションを付けるときのための機能です。<br>
リアクションピッカーはタブ状にカスタム絵文字、絵文字が分類されています。<br>
- ユーザー固定
- よく使うリアクション
- カテゴリ別(複数)

### ノートの下書き機能
Milkteaの独自機能で、<br>
ノートを途中で下書き保存することができます。
### ドライブ
Misskeyのドライブのファイルを表示することができます。

### ニックネームの上書き
Misskeyでは表記上のニックネームと、<br>
実際にユーザー間の会話で用いられる呼び名が異なることがありました。<br>
表示名と呼び名が異なるのは非常にややこしかったので、<br>
表面的にニックネームを上書き＆表示する機能を実装しました。<br>


## インストール方法
[GooglePlayストア](https://play.google.com/store/apps/details?id=jp.panta.misskeyandroidclient)でダウンロード&インストール

利用するインスタンスで事前にアカウントを作成してください。<br>
[はじめに](https://join.misskey.page/ja/wiki/first)
[インスタンス一覧](https://join.misskey.page/ja/wiki/instances/)

インストールが完了したらアプリを起動します。
認可画面が表示されるので、利用しようとしているインスタンスのURLを入力します。<br>
例えばmisskey.ioを利用する場合は、「misskey.io」と入力します。

app nameは自由に設定することがでます。<br>
app nameはインスタンスのバージョンによってはvia名として公開される場合があります。<br>

準備ができれば AUTHENTICATION (認証)を押します。<br>
<img src="https://user-images.githubusercontent.com/38454985/81928170-d03c8080-961f-11ea-8acc-b1d752d72de7.png" width="320px">

認証画面がブラウザに表示されるので、問題がなければ許可(Accept)を押します。<br>
もし、リダイレクトしない場合は戻るボタンを押して、「私は許可をしました」を押してください。
<img src="https://user-images.githubusercontent.com/38454985/81928454-3cb77f80-9620-11ea-839b-ea28962a0a92.png" width="320px"><br>

成功すればMilkteaにリダイレクトするので[続行(CONTINUE)]を押し完了です。<br>
<img src="https://user-images.githubusercontent.com/38454985/81928572-6c668780-9620-11ea-800a-bbb03721ce8e.png" width="320px"><br>



