package com.liskovsoft.youtubeapi.app.nsig

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liskovsoft.youtubeapi.common.api.FileApi
import com.liskovsoft.youtubeapi.common.helpers.ReflectionHelper
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper
import com.liskovsoft.youtubeapi.common.js.JSInterpret
import java.util.regex.Pattern

internal class NSigExtractor(private val playerUrl: String) {
    private val mFileApi = RetrofitHelper.create(FileApi::class.java)
    private var mNFunc: ((List<String>) -> String?)? = null
    private val mNFuncPatternUrl: String = "https://github.com/yuliskov/SmartTube/releases/download/latest/nfunc_pattern.txt"
    private var mNFuncPattern: Pattern? = Pattern.compile("""(?x)
            (?:
                \.get\("n"\)\)&&\(b=|
                (?:
                    b=String\.fromCharCode\(110\)|
                    ([a-zA-Z0-9${'$'}.]+)&&\(b="nn"\[\+\1\]
                ),c=a\.get\(b\)\)&&\(c=
            )
            ([a-zA-Z0-9$]+)(?:\[(\d+)\])?\([a-zA-Z0-9]\)""", Pattern.COMMENTS)

    init {
        initNFuncCode()

        if (mNFunc == null) {
            val nFuncPattern = RetrofitHelper.get(mFileApi.getContent(mNFuncPatternUrl))?.content
            nFuncPattern?.let {
                mNFuncPattern = Pattern.compile(nFuncPattern, Pattern.COMMENTS)
                initNFuncCode()
            }
        }

        if (mNFunc == null) {
            ReflectionHelper.dumpDebugInfo(javaClass, loadPlayer())
        }
    }

    fun extractNSig(nParam: String): String? {
        val func = mNFunc ?: return null

        return func(listOf(nParam))
    }

    /**
     * yt_dlp.extractor.youtube.YoutubeIE._extract_n_function_code
     *
     * yt-dlp\yt_dlp\extractor\youtube.py
     */
    private fun initNFuncCode() {
        val jsCode = loadPlayer() ?: return

        val funcName = extractNFunctionName(jsCode) ?: return

        val funcCode = JSInterpret.extractFunctionCode(jsCode, funcName) ?: return

        // store nFunc in cache
        mNFunc = JSInterpret.extractFunctionFromCode(funcCode.first, funcCode.second)
    }

    /**
     * yt_dlp.extractor.youtube.YoutubeIE._extract_n_function_name
     *
     * yt-dlp\yt_dlp\extractor\youtube.py
     */
    private fun extractNFunctionName(jsCode: String): String? {
        val nFuncPattern = mNFuncPattern ?: return null
        val nFuncMatcher = nFuncPattern.matcher(jsCode)

        if (nFuncMatcher.find() && nFuncMatcher.groupCount() >= 2) {
            val funcName = nFuncMatcher.group(nFuncMatcher.groupCount() - 1)
            val idx = nFuncMatcher.group(nFuncMatcher.groupCount())

            val escapedFuncName = Pattern.quote(funcName)

            val nameArrPattern = Pattern.compile("""var $escapedFuncName\s*=\s*(\[.+?\])\s*[,;]""")

            val nameArrMatcher = nameArrPattern.matcher(jsCode)

            if (nameArrMatcher.find() && nameArrMatcher.groupCount() == 1) {
                val nameArrStr = nameArrMatcher.group(1)

                val gson = Gson()
                val listType = object : TypeToken<List<String>>() {}.type
                val nameList: List<String> = gson.fromJson(nameArrStr, listType)

                return nameList[idx.toInt()]
            }
        }

        return null
    }

    private fun loadPlayer(): String? {
        return RetrofitHelper.get(mFileApi.getContent(playerUrl))?.content
    }
}