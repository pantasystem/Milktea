# EmojiAutoCompleteTextField 純粋 Compose 化 実装計画

このドキュメントは Claude が作業を進めるための実装計画書です。
完了したタスクは `- [ ]` を `- [x]` に変えてください。
各フェーズに着手する前に「前提条件」を確認し、完了後は「完了確認」を実施してください。

> **位置づけ**
> 本プランは [`note-editor-compose-migration-plan.md`](./note-editor-compose-migration-plan.md) の Phase 2 が
> 「まず `AndroidView` でブリッジし、安定後に純粋 Compose 実装に移行する（別フェーズ）」と明記して
> 先送りした、その**別フェーズ**にあたる。
> 現状 `EmojiAutoCompleteTextField` は `MultiAutoCompleteTextView` を `AndroidView` でラップした
> ブリッジ実装で安定稼働している。本プランではこれを純粋 Compose（`BasicTextField` + 自前補完ドロップダウン）へ置き換える。

---

## 基本方針

- **段階的移行**：CW フィールド（補完あり・単純）を先に純粋 Compose 化して足場を作り、その後で
  本文フィールド（補完＋URL貼り付け検出＋auto-focus）へ広げる。各フェーズ終了後にビルド・動作確認できる状態を保つ。
- **状態の一本化**：現状 AndroidView ブリッジのために散らばっている複数のハックを、Compose の
  `TextFieldValue`（text + selection を一体で保持）に統合する。移行のゴールは「機能追加」ではなく
  「等価な挙動をより単純な状態管理で再現する」こと。
- **既存の再利用**：`CustomEmojiTokenizer` のトークン検出ロジックと `CustomEmojiRepository.search()`（suspend）、
  候補描画用の `common_compose/CustomEmojiText.kt` は流用する。
- **ロールバック容易性**：`EmojiAutoCompleteTextField` の**公開シグネチャは変えない**方針とし、内部実装のみ差し替える。
  問題があれば内部を AndroidView 版へ戻すだけで復旧できる状態を維持する。

---

## 現状のアーキテクチャと「畳むべきハック」

現在の `EmojiAutoCompleteTextField`（`modules/features/note/.../editor/EmojiAutoCompleteTextField.kt`）は
`SelectionAwareMultiAutoCompleteTextView`（`MultiAutoCompleteTextView` のサブクラス）を `AndroidView` でラップしている。

AndroidView ブリッジのために存在している状態管理と、Compose 化後の対応表：

| 現状のハック | 目的 | 純粋 Compose 化後 |
|---|---|---|
| `SelectionAwareMultiAutoCompleteTextView.onSelectionChanged` | カーソル位置の追跡 | `TextFieldValue.selection` が text と一体で持つ → 追跡クラス不要 |
| `textCursorPosFlow`（ViewModel→View 逆流） | 絵文字挿入後のカーソル移動 | `onValueChange` 一方向フローに統一。挿入は `TextFieldValue` を直接組み立て |
| `update {}` の `text.toString() != value` ガード＋`setSelection` | text 差分同期 | 不要（state が単一の源） |
| Activity 側 `textCursorPosition` / `cwCursorPosition` の二重管理 | 絵文字ピッカー挿入位置 | フィールドの `selection` から直接算出 |
| `onTextChanged` の start/count による URL 貼り付け検出 | URL 検出 | 新旧 `TextFieldValue` の差分から判定 |
| `post { requestFocus(); WindowInsetsController.show(ime()) }` | auto-focus + IME 表示 | `FocusRequester` + `SoftwareKeyboardController` |
| `CustomEmojiCompleteAdapter` + `Filter` + `runBlocking` | 補完候補の取得と表示 | `snapshotFlow` + debounce + suspend `search()` + 自前 `Popup` |

> **重要**：上表の「二重管理」を減らすには、Activity（`NoteEditorActivity`）側の `onSelect`（絵文字ピッカー
> コールバック）が参照している `textCursorPosition` / `cwCursorPosition` の受け渡し方も見直す必要がある。
> ピッカー挿入は「現在フォーカス中フィールドの `selection` に挿入」という形へ寄せる（Phase 4 で扱う）。

---

## フェーズ一覧

| フェーズ | 内容 | リスク | 完了後にリリース可能？ |
|---------|------|--------|----------------------|
| Phase 0 | 現状挙動のスナップショット（受け入れ基準の確定） | 低 | Yes |
| Phase 1 | 補完なしの純粋 Compose テキストフィールド土台 | 低 | Yes |
| Phase 2 | 補完ドロップダウン（トークン検出＋検索＋候補UI） | 中 | Yes |
| Phase 3 | CW フィールドを純粋 Compose 版へ切替 | 低 | Yes |
| Phase 4 | 本文フィールド切替（URL検出＋auto-focus＋ピッカー挿入） | 中 | Yes |
| Phase 5 | AndroidView 版と旧ハックの削除・クリーンアップ | 低 | Yes |

---

## Phase 0: 現状挙動のスナップショット（受け入れ基準）

**ゴール:** 移行後に「挙動パリティ」を判定できるよう、現状の期待動作を明文化する。実装変更はしない。

**前提条件:** なし

以下を実機で確認し、期待挙動としてこのドキュメント下部の「受け入れ基準チェックリスト」を最終化する：

- [ ] 本文フィールドを開いた瞬間に auto-focus され IME が表示される
- [ ] `:emoji` と入力すると候補が出て、選択すると `:name:` に置換されカーソルが直後へ移動する
- [ ] カーソルを中間へタップ移動してから絵文字ピッカーで絵文字を選ぶと、その位置に挿入される
- [ ] CW を有効化したときの CW フィールドでも補完が動く
- [ ] URL を貼り付けると URL 貼り付け検出コールバックが発火する（ファイル添付確認ダイアログ等の既存挙動）
- [ ] 返信/引用/下書き復元/共有インテントでの初期テキストが正しく反映される
- [ ] 画面回転や `SavedStateHandle` 経由の復元でテキスト・カーソルが失われない

**完了確認:** 受け入れ基準チェックリスト（末尾）が確定している。

---

## Phase 1: 補完なしの純粋 Compose テキストフィールド土台

**ゴール:** `BasicTextField` ベースで、補完を除く入力機能（複数行・ヒント・スタイル・カーソル同期）を持つ
内部コンポーネントを作る。まだ既存フィールドには接続しない。

**前提条件:** Phase 0 完了

### 作成物

- `NoteEditorTextField.kt`（新規・internal）
  - `TextFieldValue` を状態に持つ `BasicTextField` ラッパー
  - `decorationBox` でヒント（プレースホルダ）表示、既存 M3 テーマ色に合わせる
  - `KeyboardOptions`（複数行）、`minLines` を受ける
  - 外部 `value: String` ⇄ 内部 `TextFieldValue` の橋渡し（selection を保持したまま text だけ同期）

### 実装メモ

- 外部 API は当面 `value: String` / `onValueChange: (String) -> Unit` のままにし、内部で `TextFieldValue` を保持する。
  外から text を差し替えるケース（ViewModel 復元・絵文字挿入）に備え、`value` 変化時は
  「text が異なる場合のみ」内部 value を更新し、selection は可能な範囲で維持する。
- `AndroidView` を `LazyColumn` に入れると再測定問題があったが、純粋 Compose では解消。ただし
  既存レイアウト（`verticalScroll` + `Column`）はそのまま維持して差分を小さくする。

### チェックリスト

- [ ] `NoteEditorTextField.kt` を作成（補完なし・複数行・ヒント対応）
- [ ] Preview で表示・入力・複数行・ヒント表示を確認
- [ ] `./gradlew :modules:features:note:compileDebugKotlin` が通る

**完了確認:** 単体 Preview で入力できる。既存フィールドには未接続なので本番挙動は不変。

---

## Phase 2: 補完ドロップダウン

**ゴール:** `:query` トークンを検出して候補を出し、選択でトークンを置換する補完機構を Phase 1 の土台に載せる。
これが本移行の唯一のまとまった作業。

**前提条件:** Phase 1 完了

### 構成

1. **トークン検出**（`CustomEmojiTokenizer` のロジックを Compose 向けに移植）
   - `TextFieldValue`（text + cursor）からカーソル直前の `:` を探し、`:` 以降カーソルまでを query とする
   - 既存 Tokenizer の `findTokenStart` / `findTokenEnd` の境界挙動（連続 `:`、末尾）を踏襲
   - 純粋関数として切り出しユニットテスト可能にする（`EmojiTokenScanner` 等）

2. **候補取得**
   - `snapshotFlow { currentQuery }` → `debounce(N ms)` → `customEmojiRepository.search(host, query)`（suspend）
   - 現状 Adapter の `runBlocking` を廃し、正しい非同期に置き換える
   - アカウント（host）変更時はクエリを再評価

3. **候補UI**
   - フィールド下に `Popup`（または `DropdownMenu`）で候補リストを表示（現状 AutoCompleteTextView も
     フィールド下表示のため、キャレット追従は不要 → 実装容易）
   - 各行は `common_compose/CustomEmojiText.kt` で絵文字画像＋名前を描画
   - 候補選択で query トークンを `:name:` に置換し、カーソルを直後へ

### チェックリスト

- [ ] `EmojiTokenScanner`（純粋関数）を作成し、Tokenizer 相当の境界ケースをユニットテスト
- [ ] `snapshotFlow` + `debounce` + suspend `search()` で候補取得を実装（`runBlocking` を使わない）
- [ ] `Popup` ベースの候補リスト UI を実装し、`CustomEmojiText` で行を描画
- [ ] 候補選択でトークン置換＋カーソル移動が正しく動く
- [ ] Preview / 単体画面で補完の一連の流れを確認
- [ ] `./gradlew :modules:features:note:compileDebugKotlin` が通る

**完了確認:** 補完付きフィールド単体で `:emoji` 入力→候補→選択→置換が動く。既存フィールドには未接続。

---

## Phase 3: CW フィールドを純粋 Compose 版へ切替

**ゴール:** `NoteEditorTextInputSection` の CW フィールドを、`EmojiAutoCompleteTextField`（AndroidView 版）から
Phase 1+2 の純粋 Compose 版へ差し替える。本文フィールドは AndroidView 版のまま残す（並行稼働）。

**前提条件:** Phase 2 完了

CW フィールドは補完はあるが URL 検出・auto-focus・ピッカー挿入位置管理がなく最も単純なため、
最初の実戦投入先として最適。

### チェックリスト

- [ ] `NoteEditorTextInputSection` の CW 側を純粋 Compose 版に差し替え
- [ ] CW の補完・入力・カーソル・`onFocused` 通知が従来通り動くことを実機確認
- [ ] CW → 本文のフォーカス移動、`focusedField` 状態更新が壊れていないことを確認
- [ ] `./gradlew :modules:features:note:compileDebugKotlin` が通る

**完了確認:** CW フィールドが純粋 Compose で動作。本文は AndroidView 版のまま無変更で共存。

---

## Phase 4: 本文フィールド切替（URL検出＋auto-focus＋ピッカー挿入）

**ゴール:** 本文フィールドを純粋 Compose 版へ切替。CW との差分機能を移植する。

**前提条件:** Phase 3 完了

### 移植する差分機能

1. **URL 貼り付け検出**
   - 現状 `onTextChanged` の start/count 依存を、新旧 `TextFieldValue` の差分（挿入区間の抽出）に置き換え
   - `UrlPatternChecker.isMatch` はそのまま利用
   - コールバック `onUrlPasted(text, start, beforeText, count)` のシグネチャは維持

2. **auto-focus + IME 表示**
   - 現状の `post { requestFocus(); WindowInsetsController.show(ime()) }` を
     `FocusRequester.requestFocus()` + `SoftwareKeyboardController.show()` へ置換
   - `windowSoftInputMode="adjustNothing"` 環境での表示可否を実機確認（今回のバグの再発防止）

3. **絵文字ピッカーからの挿入位置**
   - `NoteEditorActivity.onSelect` が参照する `textCursorPosition` / `cwCursorPosition` を、
     「フォーカス中フィールドの現在 `selection` に挿入」する形へ寄せる
   - ViewModel の `addEmoji(emoji, pos)` はそのまま活用。挿入後カーソル位置の反映を
     `textCursorPosFlow` 逆流ではなく `TextFieldValue` 更新で行う

### チェックリスト

- [ ] 本文側を純粋 Compose 版へ差し替え
- [ ] URL 貼り付け検出が従来通り発火する（ファイルサイズ/添付確認等の既存挙動を確認）
- [ ] 画面を開いた瞬間の auto-focus + IME 表示が動く（adjustNothing 環境で確認）
- [ ] カーソル中間移動 → ピッカー挿入がその位置に入る
- [ ] 返信/引用/下書き/共有インテントの初期テキスト反映を確認
- [ ] 画面回転・SavedState 復元でテキスト/カーソルが保持される
- [ ] `./gradlew :modules:features:note:compileDebugKotlin` が通る

**完了確認:** 受け入れ基準チェックリストを全て満たす。

---

## Phase 5: クリーンアップ

**ゴール:** 不要になった AndroidView ブリッジと旧ハックを削除する。

**前提条件:** Phase 4 完了かつ一定期間の動作確認

### 削除・整理候補

- [ ] `SelectionAwareMultiAutoCompleteTextView`（onSelectionChanged 追跡サブクラス）
- [ ] `EmojiAutoCompleteTextField` 内の `AndroidView` factory/update ブロック
- [ ] `textCursorPosFlow` 逆流の経路（ViewModel 側の該当 flow・emit）
- [ ] Activity 側 `textCursorPosition` / `cwCursorPosition` の二重管理（Phase 4 で寄せた後）
- [ ] `CustomEmojiCompleteAdapter`（`Filter` 版）※他に参照がないことを確認してから
- [ ] `CustomEmojiTokenizer`（Compose 版 `EmojiTokenScanner` へ置換後、他参照が無ければ）

> `CustomEmojiTokenizer` は `common_android` にあり他モジュールから参照される可能性がある。
> 削除前に全参照を grep で確認すること。

### チェックリスト

- [ ] 参照調査（grep）で安全に削除できるものだけ削除
- [ ] 未使用 import / 未使用パラメータの整理
- [ ] `./gradlew :modules:features:note:compileDebugKotlin` が通る
- [ ] `./gradlew :app:assembleDebug` が通る

**完了確認:** ビルドが通り、受け入れ基準を維持したまま旧経路が消えている。

---

## リスクと対策

| リスク | 影響 | 対策 |
|---|---|---|
| トークナイズの境界ケース差異 | 補完位置ズレ・置換ミス | `EmojiTokenScanner` を純粋関数化しユニットテストで旧挙動と突き合わせ |
| adjustNothing 環境で IME が出ない | 今回直したバグの再発 | Phase 4 で実機確認を必須化。`SoftwareKeyboardController` + `FocusRequester` で明示表示 |
| ピッカー挿入位置の回帰 | 誤位置挿入の再発 | `selection` を単一の源にし、二重管理を Phase 4 で解消 |
| 候補検索の非同期化に伴う race | 古いクエリ結果の表示 | `snapshotFlow` + `debounce` + 最新クエリのみ採用（collectLatest 相当） |
| SavedState 復元との整合 | 復元時のテキスト消失 | `value: String` 外部契約を維持し、既存の保存経路を変えない |

---

## 受け入れ基準チェックリスト（移行完了の判定）

- [ ] 本文を開いた瞬間に auto-focus + IME 表示（adjustNothing 環境）
- [ ] `:emoji` 補完：候補表示 → 選択 → `:name:` 置換 → カーソル直後移動
- [ ] カーソル中間移動後のピッカー挿入がその位置に入る
- [ ] CW フィールドの補完が動く
- [ ] URL 貼り付け検出コールバックが発火する
- [ ] 返信/引用/下書き/共有インテントの初期テキスト反映
- [ ] 画面回転・SavedState 復元でテキスト/カーソル保持
- [ ] 旧 AndroidView 経路・二重管理ハックが削除されている（Phase 5）

---

## 参考：対象ファイル

| 内容 | パス |
|------|------|
| 現状の入力欄（AndroidView 版） | `modules/features/note/src/main/java/net/pantasystem/milktea/note/editor/EmojiAutoCompleteTextField.kt` |
| 入力セクション | `modules/features/note/src/main/java/net/pantasystem/milktea/note/editor/NoteEditorTextInputSection.kt` |
| 補完アダプタ（Filter 版） | `modules/features/note/src/main/java/net/pantasystem/milktea/note/editor/CustomEmojiCompleteAdapter.kt` |
| トークナイザ | `modules/common_android/src/main/java/net/pantasystem/milktea/common_android/ui/text/CustomEmojiTokenizer.kt` |
| 絵文字描画（Compose） | `modules/common_compose/src/main/java/net/pantasystem/milktea/common_compose/CustomEmojiText.kt` |
| 絵文字挿入ロジック | `modules/features/note/src/main/java/net/pantasystem/milktea/note/editor/viewmodel/NoteEditorViewModel.kt`（`addEmoji`） |
| ピッカーコールバック | `modules/features/note/src/main/java/net/pantasystem/milktea/note/NoteEditorActivity.kt`（`onSelect`） |
| 先行プラン | `docs/note-editor-compose-migration-plan.md` |
