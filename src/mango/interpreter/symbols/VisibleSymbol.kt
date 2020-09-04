package mango.interpreter.symbols

import mango.interpreter.binding.Namespace

interface VisibleSymbol {
    val path: String

    val namespace get() = Namespace[path.substringBeforeLast('.')]

    fun mangledName(): String {
        this as Symbol
        if (meta.cname != null) return meta.cname!!
        if (this is CallableSymbol) {
            if (meta.isEntry) return "main"
            if (parameters.isNotEmpty()) {
                return path + suffix
            }
        }
        return path
    }
}
