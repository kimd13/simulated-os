package structs

sealed class FrameValue{
    var words: IntArray = IntArray(FRAME_SIZE)

    companion object{
        @JvmStatic
        val FRAME_SIZE = 512
        //must be accessed by Physical Memory
    }
}

class Page: FrameValue(){
    override fun toString(): String {
        return "PAGE"
    }
}

class PageTable: FrameValue(){
    override fun toString(): String {
        return "PAGE TABLE"
    }
}

class SegmentTable: FrameValue(){
    override fun toString(): String {
        return "SEGMENT TABLE"
    }
}