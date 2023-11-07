# Contribution Guide
Milkteaにコントリビュートするためのガイドです。  

## Issue
下記のIssueを受け付けています。  
- 不具合報告
- 新機能要望
- 機能強化要望
- 質問

## Pull Request
Pull Requestはいつでも大歓迎です！  
IssueからPull Requestを作成するときは、必ずAssignするようにしてください。  
Pull Requestを作成するときは下記ルールに従い作成するようにしてください。  
- ライセンスや法を遵守したコードであること
- アーキテクチャやコーディング規約が存在する場合はそれらに従ったコードを書くこと
- テンプレートに従いPRを作成すること
- 動作するコードであること（CI/CDが完了する＆エミュレーターで動作することを確認してください)

## ブランチの命名規則
### 機能開発&リファクタリング
Issueが存在しない場合はfeatureで初めてスラッシュで区切って、作業名や機能名で分割するようにしてください  
`feature/機能名`  
機能ブランチかつIssueが存在する場合はfeatureスラッシュ、で初めて次にシャープIssue番号、次にスラッシュで区切って作業名や機能名で分割するようにしてください。  
`feature/#Issue番号/機能名`

### 不具合修正系
hotfixで始めるようにして、Issueが存在する場合はIssue番号を入れて、
存在しない場合はそのまま修正する機能名や不具合名で始めるようにしてください。  
`hotfix/#Issue番号/機能名or不具合名`  
`hotfix/機能名or不具合名`

# アーキテクチャ
現在MilkteaではMVVMアーキテクチャをベースとしたアーキテクチャを採用しています。  
非同期系の処理にはCoroutinesを採用しています。  
リアクティブ系の処理にはCoroutines Flowを使用して処理をしています。  

## ディレクトリ構成
Milkteaではマルチモジュール構成になっていて、以下のような構造になっています。
```
.  
├──app  
├──modules  
   ├──api  
   ├──api_streaming
   ├──app_store
   ├──common
   ├──common_android
   ├──common_android_ui
   ├──common_compose
   ├──common_navigation
   ├──common_resource
   ├──common_viewmodel
   ├──data
   ├──features
      ├──antenna
      ├──auth
      ├──channel
      ├──drive
      ├──favorite
      ├──gallery
      ├──group
      ├──media
      ├──messaging
      ├──note
      ├──notification
      ├──search
      ├──setting
      ├──user
      ├──userlist
   ├──model
```
### app
MainActivityなどの初回起動系のActivityやその関連の処理が入っています。
### modules
各種モジュールが入っているモジュールディレクトリです。
### api
Retrofit2のインターフェースや、通信のためのDTOなどのオブジェクトがここに入っています。
### api_streaming
MisskeyのStreaming APIに関する処理のモジュールです。
### app_store
アプリ全体で共有したい状態を管理する機能のことをMilkteaでは.*Storeと呼んでいて、
app_storeモジュールはそれら機能を配置するためのモジュールです。
### common
Android, プロジェクトに依存しないような共通で使われる機能用のモジュールです。
### common_android
Androidに関する共通機能を内包したモジュールです。  
UIに関する機能は後述するcommon_android_uiかcommon_composeに分類しています。

### common_android_ui
AndroidのUIに関する共通機能などをここに分類しています。　
### common_compose
Jetpack Composeに関する共通機能をここに分類しています。
### common_navigation
マルチモジュールを実現するために、遷移先の実装を抽象化する必要がありました。
common_navigationは画面繊維や、その抽象のためのインターフェースが分類されています。

### common_resource
drawableやstringsなどで共通して使いたいリソースをここに分類しています。

### common_viewmodel
本来ViewModelは画面:1で存在するべきものですが、  
確認ダイアログやアプリ全体で共通して使う必要のあるViewModel実装をここに分類しています。  
基本的には.*Storeで事足りるので、滅多に使わないものだと思ってください。

### data
API通信やDB処理に関する実装クラスがここに入っています。  
後述するmodelに作成された抽象をここで実装することが多いです。  

### features
このディレクトリには、各種機能ごとのUIのモジュールが格納されています。  
各種機能のモジュールには、viewmodelやそのUIに関する処理が格納されています。  

### model
ビジネスロジックや、APIやDB処理やキャッシュ処理などの抽象がここに入っています。  
一般的にはここに抽象が作成され、dataモジュールで実装され、HiltというDIコンテナーで依存性の解決を行うことが多いです。

## 代表的な役割クラス
MilkteaではMVVMアーキテクチャを採用しています。  
その中でもMilktea内でよく使われる概念や用語の説明をします。  

### リソース名(Entity)
UserやNoteなどのリソース名を直接指定したオブジェクトをEntityと呼称しています。  
主にそのリソースに関するフィールドを持っていたり、そのデータに関連する振る舞いの実装を持っていることが多いです。  

### リソース名Repository(Repository)
永続化処理に関する抽象クラスです。  
主にAPIへの取得、更新リクエストや、DBへの取得更新リクエストを行なっています。  
またMilkteaでは後述するDataSourceというサーバキャッシュ層を持っており、それらの更新をRepositoryが担っていたりします。  

### リソース名DataSource(DataSource)
サーバーから取得したデータ(Entity)のキャッシュを行なっています。  
実際には実装クラスがあり、メモリ上やSQLite上に実装されることがあります。

### リソース名Relation
複数のEntityを組み合わせた構造体です。

### 機能名,画面名ViewModel(ViewModel)
ViewModelです。

### 機能名,画面名Activity(Activity)
Activityです。

### 機能名,画面名Fragment(Fragment)
Fragmentです。

### 要素名Adapter(RecyclerView.Adapter)
RecyclerView.Adapterの実装クラスです。  
ごく稀に諸事情でListViewのAdapter実装クラスの場合もあります。  

### リソース名ViewData
EntityやRelationをよりViewに最適化した構造のことをViewDataと呼称しています。  
レイヤーとしてはViewModelやアプリケーション層に分類されると思っています。

## ビルドするには

プロジェクトをgit cloneします。  
secret.propertiesを作成します。  
```
touch secret.properties
```
secret.propertiesには  
以下のような属性を追加してプッシュ通知の中継鯖についての設定をします。  
プッシュ通知中継サーバについて  
https://github.com/pantasystem/MisskeyAndroidClient/blob/develop/PushToFCM/README.md  

push_to_fcm.server_base_urlにはプッシュ通知サーバのベースURLを設定します。  
push_to_fcm.public_keyにはPushToFCMで生成したpublicを設定します。  
push_to_fcm.authにはPushToFCMで生成したauthを設定します。  

```
push_to_fcm.server_base_url=https://hogehogehoge-pus
push_to_fcm.public_key=中継鯖（PushToFCM）に設定したpublic_keyを設定します
push_to_fcm.auth=中継鯖に設定したauth_secret.txtを設定します
```
Android SDK, AndroidStudioでビルドします。  