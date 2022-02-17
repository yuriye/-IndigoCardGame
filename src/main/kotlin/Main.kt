import kotlin.system.exitProcess

const val ALL_CARDS_NAMES =
    "A♠ 2♠ 3♠ 4♠ 5♠ 6♠ 7♠ 8♠ 9♠ 10♠ J♠ Q♠ K♠ A♥ 2♥ 3♥ 4♥ 5♥ 6♥ 7♥ 8♥ 9♥ 10♥ J♥ Q♥ K♥ A♦ 2♦ 3♦ 4♦ 5♦ 6♦ 7♦ 8♦ 9♦ 10♦ J♦ Q♦ K♦ A♣ 2♣ 3♣ 4♣ 5♣ 6♣ 7♣ 8♣ 9♣ 10♣ J♣ Q♣ K♣"
val INITIAL_DECK = ALL_CARDS_NAMES.split(" ").toList()

class Card(var rank: String, var suit: String) {

    var name: String
        set(value) {
            this.rank = value.substring(0 until value.lastIndex)
            this.suit = value.substring(value.lastIndex..value.lastIndex)
        }
        get() = this.rank + this.suit

    constructor(name: String) : this("", "") {
        this.name = name
    }
}

fun moveCards(number: Int, from: MutableList<Card>, to: MutableList<Card>) {
    for (counter in 0 until number) {
        to.add(from[counter])
        if (counter == from.lastIndex) break
    }
    for (counter in 0 until number) from.removeFirst()
}

fun makeMultiLists(list: MutableList<Card>): Array<MutableList<Card>> {

    val suitMap = mutableMapOf<String, Int>()
    val rankMap = mutableMapOf<String, Int>()

    for (card in list) {
        suitMap.set(card.suit,
            if (suitMap.containsKey(card.suit)) suitMap.get(card.suit)!! + 1 else 1)
        rankMap.set(card.rank,
            if (rankMap.containsKey(card.rank)) rankMap.get(card.rank)!! + 1 else 1)
    }

    val suitMultyList = mutableListOf<Card>()
    val rankMultyList = mutableListOf<Card>()

    for (card in list) {
        if (suitMap.get(card.suit)!! > 1) {
            suitMultyList.add(card)
        }
        if (rankMap.get(card.rank)!! > 1) {
            rankMultyList.add(card)
        }
    }

    return arrayOf(suitMultyList, rankMultyList)
}

class Game {

    var firstMoveOfComp = false

    var lastMoveOfComp = false;

    var deck = mutableListOf<Card>()

    val table = mutableListOf<Card>()

    val compHand = mutableListOf<Card>()

    val compCards = mutableListOf<Card>()

    val playerHand = mutableListOf<Card>()

    val playerCards = mutableListOf<Card>()

    var lastWinerComp = false

    var wasWiner = false

    fun deal() {
        moveCards(6, deck, compHand)
        moveCards(6, deck, playerHand)
    }

    fun turnCard(hand: MutableList<Card>, index: Int) {
        table.add(hand[index])
        compHand.removeAt(index)
    }

    fun turnCard(hand: MutableList<Card>, card: Card) {
        table.add(card)
        compHand.remove(card)
    }

    fun winsCard(card: Card): Boolean {
        if (table.size == 0) return false
        return card.rank == table[table.lastIndex].rank || card.suit == table[table.lastIndex].suit
    }

    fun getCardByStrategy1(hand: MutableList<Card>): Card {
        val card: Card
        val multiArray = makeMultiLists(hand)
        if (multiArray[0].size > 0) {
            card = multiArray[0].random()
        } else if (multiArray[1].size > 0) {
            card = multiArray[1].random()
        } else {
            card = compHand.random()
        }
        return card
    }

    fun getCardByStrategy2(card: Card, candidates: MutableList<Card>): Card {

        val listSuit = mutableListOf<Card>()
        val listRank = mutableListOf<Card>()

        for (candCard in candidates) {
            if (candCard.suit == card.suit) listSuit.add(candCard)
            if (candCard.rank == card.rank) listRank.add(candCard)
        }

        if (listSuit.size > 0) return listSuit.random()
        if (listRank.size > 0) return listRank.random()
        return candidates.random()

    }

    fun computerMove() {

        var strCompHand = ""
        for (card in compHand) strCompHand += card.name + " "
        println(strCompHand.substring(0..strCompHand.lastIndex - 1))

        var card = compHand[0]
        val candidates = mutableListOf<Card>()

        if (compHand.size > 1) {
            for (cardCandidate in compHand) {
                if (winsCard(cardCandidate)) candidates.add(cardCandidate)
            }
        }

        when {

            compHand.size == 1 -> card = compHand[0]

            candidates.size == 1 -> card = candidates[0]

            table.size == 0 || table.size > 0 && candidates.size == 0 -> card = getCardByStrategy1(compHand)

            candidates.size >= 2 -> card = getCardByStrategy2(table[table.lastIndex], candidates)

        }

        println("Computer plays ${card.name}")
        turnCard(compHand, card)
        lastMoveOfComp = true
    }

    fun printTableStatistics() {
        val message = if (table.size == 0) "No cards on the table"
        else "${table.size} cards on the table, and the top card is ${table[table.lastIndex].name}"
        println(message)
    }

    fun playerMove() {

        var strOnHand = ""
        for (i in 0..playerHand.lastIndex) strOnHand += " ${i + 1})${playerHand[i].name}"
        println("Cards in hand:$strOnHand")
        var index = -1

        while (true) {

            println("Choose a card to play (1-${playerHand.size}):")
            val answer = readLine()!!
            if (answer.uppercase() == "EXIT") {
                println("Game Over")
                exitProcess(0)
            }

            try {
                index = answer.toInt() - 1
            } catch (e: Exception) {
                continue
            }
            if (index in 0..playerHand.lastIndex) break

        }

        table.add(playerHand[index])
        playerHand.removeAt(index)
        lastMoveOfComp = false
    }

    fun testForWins(): Boolean {

        if (table.size < 2) {
            return false
        }

        if ((table[table.lastIndex].rank == table[table.lastIndex - 1].rank) ||
            (table[table.lastIndex].suit == table[table.lastIndex - 1].suit)
        ) {
            wasWiner = true
            val destination = if (lastMoveOfComp) compCards else playerCards
            lastWinerComp = if (lastMoveOfComp) true else false
            destination.addAll(table)
            table.clear()

            val message = if (lastMoveOfComp) "Computer wins cards" else "Player wins cards"
            println(message)
            return true
        }

        return false
    }

    fun printScores(final: Boolean = false) {

        var playerScore = 0

        for (card in playerCards) playerScore += if ("A 10 J Q K ".contains(card.rank + " ")) 1 else 0

        var compScore = 0
        for (card in compCards) compScore += if ("A 10 J Q K ".contains(card.rank + " ")) 1 else 0

        if (final) {
            when {
                (playerCards.size == compCards.size) -> {
                    if (firstMoveOfComp) compScore += 3
                    else playerScore += 3
                }
                (playerCards.size > compCards.size) -> {
                    playerScore += 3
                }
                else -> compScore += 3
            }
        }

        println("Score: Player $playerScore - Computer $compScore")
        println("Cards: Player ${playerCards.size} - Computer ${compCards.size}")
    }

    constructor() {
        deck = mutableListOf<Card>()
        for (name in INITIAL_DECK) deck.add(Card(name))
        deck.shuffle()
        moveCards(4, deck, table)
        deal()
    }

    fun run() {

        println("Indigo Card Game")

        while (true) {
            println("Play first?")
            val answer = readLine()!!.uppercase()
            if (answer == "YES") {
                lastMoveOfComp = true
                firstMoveOfComp = false
                break
            } else if (answer == "NO") {
                lastMoveOfComp = false
                firstMoveOfComp = true
                break
            }
        }

        println("Initial cards on the table: ${table[0].name} ${table[1].name} ${table[2].name} ${table[3].name}")

        while (true) {

            printTableStatistics()

            if (lastMoveOfComp) playerMove()
            else computerMove()

            val wins = testForWins()

            if (wins) printScores()

            var final = deck.size + playerHand.size + compHand.size == 0

            if (final) {

                printTableStatistics()

                if (wasWiner) {
                    if (lastWinerComp) compCards.addAll(table)
                    else playerCards.addAll(table)
                } else {
                    when {
                        (compCards.size > playerCards.size) -> compCards.addAll(table)
                        (compCards.size < playerCards.size) -> playerCards.addAll(table)
                        else -> {
                            if (firstMoveOfComp) compCards.addAll(table)
                            else playerCards.addAll(table)
                        }
                    }
                }

                printScores(true)
                println("Game over")
                return
            }


            if (compHand.size + playerHand.size == 0) deal()
        }
    }
}

fun main() {
    val game = Game()
    game.run()
}