package jp.panta.misskeyandroidclient.mfm

import jp.panta.misskeyandroidclient.PostNoteService.Companion.tag
import java.util.regex.Pattern

object MFMParser{

    fun parse(text: String): Node{
        println("textSize:${text.length}")
        val root = Root(text)
        NodeParser(text, root).parse()
        return root
    }


    /**
     * @param start 担当する文字列のスタート地点
     * @param end 担当する文字列の終了地点
     * @param parent 親ノードこのParserはこの parentの内側の処理をしていることになる
     * つまりNodeParserとの関係はparent : NodeParserという一対一の関係になる。
     * <parent>child content</parent>
     *         ↑start       ↑end
     */
    class NodeParser(
        val sourceText: String,
        val parent: Node,
        val start: Int = parent.insideStart,
        val end: Int = parent.insideEnd
    ){
        // タグ探索開始
        // タグ探索中
        // タグ探索完了
        // タグ探索キャンセル

        private var position: Int = start

        /**
         * 一番最後にタグを検出したタグの最後のpositionがここに代入される。
         * recoveryBeforeText()されるときに使用される
         */
        private var finallyDetected: Int = start


        /**
         * 第一段階としてタグの先頭の文字に該当するかを検証する
         * キャンセルされたときはここからやり直される
         */
        private val parserMap = mapOf(
            '<' to ::parseBlock, //斜体、小文字、中央揃え、横伸縮、左右反転、回転、飛び跳ねる
            '~' to ::parseStrike, //打消し線
            //'(' to ::parseExpansion, //横伸縮
            '`' to ::parseCode, //コード
            '>' to ::parseQuote, //引用
            '*' to ::parseTypeStar,  // 横伸縮対称揺れ, 太字
            '【' to ::parseTitle,//タイトル
            '[' to ::parseTitle

        )

        /**
         * 何にも該当しない文字を葉として追加する
         * @param tagStart 次に存在するNodeの始点
         * text<Node> この場合だと4になる
         */
        private fun recoveryBeforeText(tagStart: Int){
            // 文字数が０より多いとき
            if((tagStart - finallyDetected) > 0){
                val text = sourceText.substring(finallyDetected, tagStart)
                parent.childNodes.add(Text(text))
            }
        }

        fun parse(){
            while(position < end){
                val parser = parserMap[sourceText[position]]

                if(parser == null){
                    // 何にも該当しない場合は繰り上げる
                    position ++
                }else{

                    val node = parser.invoke()
                    // nodeが実際に存在したとき
                    if(node != null){

                        // positionは基本的にはNodeの開始地点のままなので発見したNodeの終了地点にする
                        position = node.end


                        // Nodeの直前のNodeに含まれないLeafの回収作業を行う
                        recoveryBeforeText(node.start)

                        // 新たに発見したnodeの一番最後の外側の文字を記録する
                        finallyDetected = node.end



                        // 発見したNodeを追加する
                        parent.childNodes.add(node)


                        // 新たに発見した子NodeのためにNodeParserを作成する
                        // 新たに発見した子Nodeの内側を捜索するのでparentは新たに発見した子Nodeになる
                        NodeParser(sourceText, parent = node).parse()


                    }else{
                        position ++
                    }
                }
            }
            //parent.endTag.start == position -> true
            recoveryBeforeText(parent.insideEnd)

        }

        /**
         * タグの開始位置や終了位置、内部要素の開始、終了位置は正規表現とMatcherを利用し現在のポジションと合わせ相対的に求める
         */

        private fun parseTypeStar(): Node?{
            val boldPattern = Pattern.compile("""\A\*\*(.+?)\*\*""")
            val animationPattern = Pattern.compile("""\A\*\*\*(.+?)\*\*\*""")
            val currentInside = sourceText.substring(position, parent.insideEnd)

            if(animationPattern.matcher(currentInside).find()){
                // アニメーションはサポートしていないため終了
                return null
            }

            val matcher = boldPattern.matcher(currentInside)
            if(!matcher.find()){
                return null
            }
            if(parent.tag.tagClass.weight < TagType.BOLD.tagClass.weight || parent.tag == TagType.BOLD){
                return null
            }


            return Node(
                start = position,
                end = position + matcher.end(),
                tag = TagType.BOLD,
                insideStart = position + 2,
                insideEnd = position + matcher.end() - 2,
                parentNode = parent
            )
        }

        private fun parseBlock(): Node?{
            val pattern = Pattern.compile("""\A<([a-z]+)>(.+?)</\1>""", Pattern.DOTALL)
            val matcher = pattern.matcher(sourceText.substring(position, parent.insideEnd))
            if(!matcher.find()){
                return null
            }else{
                val tagName = matcher.group(1)

                val tag = MFMContract.blockTypeTagNameMap[tagName]?: return null

                // Parentより自分のほうが重い又は同じタグの場合無効
                if(parent.tag.tagClass.weight < tag.tagClass.weight || parent.tag == tag){
                    return null
                }

                return Node(
                    start = position,
                    end = position + matcher.end(),
                    tag = tag,
                    insideStart = position + tagName.length + 2,
                    insideEnd = position + matcher.end(2),
                    parentNode = parent
                )


            }
        }

        private fun parseStrike(): Node?{
            val pattern = Pattern.compile("""\A~~(.+?)~~""")
            val matcher = pattern.matcher(sourceText.substring(position, parent.insideEnd))
            if(!matcher.find()){
                return null
            }
            if(parent.tag.tagClass.weight < TagType.STRIKE.tagClass.weight || parent.tag == TagType.STRIKE){
                return null
            }

            return Node(
                start = position,
                end = position + matcher.end(),
                tag = TagType.STRIKE,
                insideStart = position + matcher.start(1),
                insideEnd = position + matcher.start(1) + matcher.group(1).length,
                parentNode = parent
            )
        }



        private fun parseCode(): Node?{
            val pattern = Pattern.compile("""\A```(.*)```""")
            val matcher = pattern.matcher(sourceText.substring(position, parent.insideEnd))
            if(!matcher.find()){
                return null
            }
            if(parent.tag != TagType.ROOT){
                return null
            }
            return Node(
                start = position,
                end = position + matcher.end(),
                tag = TagType.CODE,
                insideStart = position + matcher.start(1),
                insideEnd = position + matcher.end(1),
                parentNode = parent
            )
        }

        private fun parseQuote(): Node?{

            // 直前の文字がある場合
            if(position > 0){
                val c = sourceText[ position - 1 ]
                // 直前の文字が改行コードではないかつ、親が引用コードではない
                if( (c != '\r' && c != '\n') && parent.tag != TagType.QUOTE){
                    println("直前の文字が改行コードではないかつ、親が引用コードではない")
                    return null
                }
                if( parent.tag.tagClass.weight < TagType.QUOTE.tagClass.weight && parent.tag != TagType.ROOT){
                    println("親ノードのほうが小さい")
                    return null
                }
            }
            val quotePattern = Pattern.compile("""^>(?:[ ]?)([^\n\r]+)(\n\r|\n)?""", Pattern.MULTILINE)
            val matcher = quotePattern.matcher(sourceText.substring(position, parent.insideEnd))


            if(!matcher.find()){
                return null
            }
            val nodeEnd = matcher.end()
            println(matcher.group(1))


            // > の後に何もない場合キャンセルする
            if(nodeEnd + position <= position){
                return null
            }
            ///println("inside:$inside")

            return Node(
                start = position,
                end = nodeEnd + position,
                tag = TagType.QUOTE,
                insideStart = position + 1, // >を排除する
                insideEnd = position + nodeEnd,
                parentNode = parent
            )
        }

        private fun parseTitle(): Node?{
            val pattern = Pattern.compile("""\A[【\[](.+?)[】\]]\n$""")
            val matcher = pattern.matcher(sourceText.substring(position, parent.insideEnd))
            if(!matcher.find()){
                return null
            }
            if(parent.tag != TagType.ROOT){
                return null
            }
            return Node(
                start = position,
                end = position + matcher.end(),
                tag = TagType.TITLE,
                insideStart = position + matcher.start(1),
                insideEnd = position + matcher.end(1),
                parentNode = parent
            )
        }


    }
}