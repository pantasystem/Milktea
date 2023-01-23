package net.pantasystem.milktea.model.instance

class Version : Comparable<Version>{

    enum class Major{
        V_10{
            override fun range(version: Version): Boolean {
                return version >= Version("10") && version < Version("11")
            }
        },
        V_11{
            override fun range(version: Version): Boolean {
                return version >= Version("11") && version < Version("12")
            }
        },
        V_12{
            override fun range(version: Version): Boolean {
                return version >= Version("12") && version < Version("13")
            }
        },
        V_13 {
            override fun range(version: Version): Boolean {
                return version >= Version("13") && version < Version("14")
            }
        };

        abstract fun range(version: Version): Boolean
    }

    private val subVersions: List<Int>

    constructor(version: String){
        subVersions = version.split(".").map{
            try{
                Integer.parseInt(it)
            }catch(e: NumberFormatException){
                0
            }
        }
    }

    constructor(meta: Meta) : this(meta.version)


    override fun compareTo(other: Version): Int {
        // 桁違いに酔って発生したスペースは０とする
        val size = other.subVersions.size.coerceAtLeast(this.subVersions.size)
        for(index in 0 until size){
            val result = this.getSubVersion(index).compareTo(other.getSubVersion(index))
            if(result != 0){
                return result
            }
        }
        return 0
    }

    private fun getSubVersion(index: Int): Int{
        return try{
            subVersions[index]
        }catch(e: IndexOutOfBoundsException){
            0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Version

        if (subVersions != other.subVersions) return false

        return true
    }

    override fun hashCode(): Int {
        return subVersions.hashCode()
    }

    fun isUntilRange(minVersion: Version, maxVersion: Version): Boolean{
        return try{
            this >= minVersion && this < maxVersion
        }catch( e: Exception ){
            false
        }
    }

    fun isInRange(version: Major): Boolean{
        return try{
            version.range(this)
        }catch( e: Exception ){
            false
        }
    }


}