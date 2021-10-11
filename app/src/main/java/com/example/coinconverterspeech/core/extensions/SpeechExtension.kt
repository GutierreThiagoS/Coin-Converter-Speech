package com.example.coinconverterspeech.core.extensions

import android.util.Log
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.data.model.CoinCod
import com.example.coinconverterspeech.data.model.ExchangeValue
import java.text.DecimalFormat

fun ExchangeValue.resultSpeech(): String{
    val coin = Coin.getByName(code)
    val prefixText =
        when (code){
            CoinCod.BRL.value -> {
                "${coinToConverter.formatCurrency(coin.locale)}} "
            }
            CoinCod.USD.value -> {
                "${coinToConverter.formatCurrency(coin.locale)} em ${name.substring(0, name.indexOf("/"))}"
            }
            CoinCod.CAD.value -> {
                "${coinToConverter.formatCurrency(coin.locale)} em ${name.substring(0, name.indexOf("/"))}"
            }
            CoinCod.ARS.value -> {
                "${coinToConverter.toDoubleString().convertPeso()} "
            }
            else -> ""
        }
    val coinIn = Coin.getByName(codein)
    val suffixText = when (codein){
        CoinCod.BRL.value -> {
            " é o que vale a ${bid.formatCurrency(coinIn.locale)}}"
        }
        CoinCod.USD.value -> {
            " é o que vale a ${bid.formatCurrency(coinIn.locale)} em ${name.substring(name.indexOf("/"), name.length)}"
        }
        CoinCod.CAD.value -> {
            " é o que vale a ${bid.formatCurrency(coinIn.locale)} em ${name.substring(name.indexOf("/"), name.length)}"
        }
        CoinCod.ARS.value -> {
            " é o que vale a ${bid.toDoubleString().convertPeso()}"
        }
        else -> ""
    }
    return prefixText + suffixText
}

fun String.convertPeso(): String{
    val peso = this
    Log.e("convertPeso", peso)
    val pesoSuffix = "e ${peso.substring(peso.indexOf(",") + 1, peso.length).toNumber()} centavos"
    Log.e("convertPeso", "pesoSuffix $pesoSuffix")
    return "${peso.substring(0, peso.indexOf(",")).toNumber()} em Peso Argentino ${if (pesoSuffix.toNumber().toInt() > 0) pesoSuffix else ""}"
}


fun String.convertValueReal(): String {
    val result = this
    val valuePre = when {
        result.contains("e") -> result.substring(0, result.indexOf("e")).toNumber()
        result.contains(":") -> result.substring(0, result.indexOf(":")).toNumber()
        result.contains(",") -> result.substring(0, result.indexOf(",")).toNumber()
        else -> result.substring(0, result.indexOf(" ")).toNumber()
    }
    val valueSuffix = when {
        result.contains("e") -> result.substring(result.indexOf("e") + 1, result.length).toNumber()
        result.contains(":") -> result.substring(result.indexOf(":") + 1, result.length).toNumber()
        result.contains(",") -> result.substring(result.indexOf(",") + 1, result.length).toNumber()
        else -> result.substring(result.indexOf(" ") + 1, result.length).toNumber()
    }
    Log.e("RESULT", "$this, valuePre $valuePre, valueSuffix $valueSuffix")
    return ("$valuePre.$valueSuffix").trim()
}

fun Double.toDoubleString(): String{
    val format = DecimalFormat("####0.00")
    return format.format(this)
}

fun String.toNumber(): String{
    var newText = replace("uma", "1")
    newText = newText.replace("um", "1")
    newText = newText.replace("hum", "1")
    newText = newText.replace("umm", "1")
    newText = newText.replace("duas", "2")
    newText = newText.replace("dois", "2")
    newText = newText.replace("três", "3")
    newText = newText.replace("quatro", "4")
    newText = newText.replace("cinco", "5")
    newText = newText.replace("seis", "6")
    newText = newText.replace("sei", "6")
    newText = newText.replace("sete", "7")
    newText = newText.replace("oito", "8")
    newText = newText.replace("nove", "9")
    newText = newText.replace("dez", "10")
    newText = newText.replace("onze", "11")
    newText = newText.replace("doze", "12")
    newText = newText.replace("cinquenta", "50")
    newText = newText.replace("sessenta", "60")
    newText = newText.replace("meia dúzia", "6")
    newText = newText.replace("meia", "6")
    newText = newText.replace("1 dúzia", "12")
    newText = newText.replace("zero", "0")
    newText = newText.replace("-", "")
    newText = newText.replace("/", "")
    newText = newText.replace("+", "")
    newText = newText.replace("$", "")
    newText = newText.replace(Regex("[^\\p{ASCII}]"), "")
    newText = newText.replace(Regex("[*a-zA-Z]"), "")
    newText = newText.replace(" ", "")
    return newText
}

fun String.wordCount(): Int{
    return split(" ").size
}