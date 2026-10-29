package com.zaacoo.katalog

import com.zaacoo.katalog.data.ChallengeSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeSolverTest {

    @Test
    fun solvesInfinityFreeChallenge() {
        val html = """
            <html><body><script type="text/javascript" src="/aes.js" ></script><script>function toNumbers(d){var e=[];d.replace(/(..)/g,function(d){e.push(parseInt(d,16))});return e}function toHex(){for(var d=[],d=1==arguments.length&&arguments[0].constructor==Array&&arguments[0]||arguments,e="",f=0;f<d.length;f++)e+=(16>d[f]?"0":"")+d[f].toString(16);return e.toLowerCase()}var a=toNumbers("f655ba9d09a112d4968c63579db590b4"),b=toNumbers("98344c2eee86c3994890592585b49f80"),c=toNumbers("a45e5db83e573457513f1a28ab46e648");document.cookie="__test="+toHex(slowAES.decrypt(c,2,a,b))+"; max-age=21600; expires=Thu, 31-Dec-37 23:55:55 GMT; path=/"; location.href="https://zaaxcoo.gt.tc/?i=1";</script><noscript>This site requires Javascript to work</noscript></body></html>
        """.trimIndent()

        assertEquals("63ffc8dd087089b8d70c73ea3faeb23c", ChallengeSolver.solve(html))
    }

    @Test
    fun returnsNullWhenChallengeIsMissing() {
        assertNull(ChallengeSolver.solve("<html><body>Bukan challenge</body></html>"))
    }
}
