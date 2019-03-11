package com.example.kotlinapp

class MainKT () {
    //val é final. Não muda o valor
    //var pode mudar o valor
//    var numero: Int = 1
//    fun main () {
//        print(numero);
//    }

    //var tem que ser inicializada
    //? eh opcional
//    var posicao: Posicao? = null


    private val posicao: Posicao? = Posicao(0.1)
    fun main() {
//        posicao = Posicao(0.1)
        posicao?.let { pos ->
            val latitude = pos.latitude
            print(latitude)
        }
    }
}

class Posicao (var latitude: Double)
