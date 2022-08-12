package watermark

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import kotlin.system.exitProcess

var baseImg = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
var watermarkImg = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
var weight = 0
var condition = ""
var backGroundColor = listOf<Int>()
var single = false
var start = mutableListOf(0,0)

fun main() {
    println("Input the image filename:")
    val imgFileName = readln()
    checkCompatibility(imgFileName)
    baseImg = ImageIO.read(File(imgFileName))
    println("Input the watermark image filename:")
    val watermarkImgFileName = readln()
    checkCompatibility(watermarkImgFileName,"watermark")
    watermarkImg = ImageIO.read((File(watermarkImgFileName)))
    if (watermarkImg.colorModel.numComponents == 4 && watermarkImg.transparency == 3) {
        println("Do you want to use the watermark's Alpha channel?")
        if (readln() == "yes") condition = "alpha"
    } else {
        println("Do you want to set a transparency color?")
        if (readln() == "yes") {
            condition = "transparent"
            println("Input a transparency color ([Red] [Green] [Blue]):")
            backGroundColor = makeRbg(readln().split(" "))
        }
    }
    println("Input the watermark transparency percentage (Integer 0-100):")
    weight = makePercentage(readln())
    println("Choose the position method (single, grid):")
    single = checkPositionMethodInput(readln())
    if (single) {
        val diff = arrayOf(baseImg.width - watermarkImg.width, baseImg.height - watermarkImg.height)
        println("Input the watermark position ([x 0-${diff[0]}] [y 0-${diff[1]}]):")
        start = makePosition(readln().split(" "),diff)
    }
    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()
    checkOutput(outputFileName)
    val blendedImg = blendImg()
    val outPutFile = File(outputFileName)
    outPutFile.createNewFile()
    ImageIO.write(blendedImg, outputFileName.substringAfterLast('.'), outPutFile)
    println("The watermarked image ${outPutFile.path} has been created.")
}
fun checkCompatibility(imgFileName: String, type: String = "image") {
    if (!File(imgFileName).exists()) error("The file $imgFileName doesn't exist.")
    val img: BufferedImage = ImageIO.read(File(imgFileName))
    if (img.colorModel.numColorComponents != 3) error("The number of $type color components isn't 3.")
    if (img.colorModel.pixelSize != 24 && img.colorModel.pixelSize != 32) error("The $type isn't 24 or 32-bit.")
    if (type == "image") return // THE PART AFTER THIS ONLY CONCERNS WATERMARK IMAGES
    if (img.width > baseImg.width || img.height > baseImg.height) error("The watermark's dimensions are larger.")
}
fun makeRbg(text: List<String>) : List<Int> {
    val errorMsg = "The transparency color input is invalid."
    if (text.size != 3) error(errorMsg)
    val color = mutableListOf<Int>()
    try {
        text.forEach { color.add(it.toInt()) }
    } catch (e: Exception) { error(errorMsg)}
    color.forEach { if (it !in 0..255) error(errorMsg) }
    return color.toList()
}
fun makePercentage(percent : String): Int {
    try { percent.toInt() } catch (e: Exception) { error("The transparency percentage isn't an integer number.")}
    if (percent.toInt() !in 0..100) error("The transparency percentage is out of range.")
    return percent.toInt()
}
fun checkPositionMethodInput(text: String): Boolean {
    if (text != "single" && text != "grid") error("The position method input is invalid.")
    return text == "single"
}
fun makePosition(text: List<String>, diff: Array<Int>): MutableList<Int> {
    if (text.size != 2) error("The position input is invalid.")
    val coordinate = mutableListOf<Int>()
    try { text.forEach { coordinate.add(it.toInt())} } catch (e: Exception) {
        error("The position input is invalid.")
    }
    if (coordinate[0] !in 0..diff[0] || coordinate[1] !in 0..diff[1] ) {
        error("The position input is out of range.")
    }
    return coordinate
}
fun checkOutput(fileName: String) {
    val format = fileName.substringAfterLast('.')
    if (format != "jpg" && format != "png") error("The output file extension isn't \"jpg\" or \"png\".")
}
fun blendImg(): BufferedImage {
    val alphaChannel = condition == "alpha"
    val transparent = condition == "transparent"
    val blendedImg = BufferedImage(baseImg.width, baseImg.height, BufferedImage.TYPE_INT_ARGB)
    for (x in 0 until baseImg.width) {
        for (y in 0 until baseImg.height) {
            val i = Color(baseImg.getRGB(x, y), alphaChannel)
            blendedImg.setRGB(x, y, Color(i.red, i.green, i.blue).rgb)
        }
    }
    val end = when (single) {
        true -> listOf(watermarkImg.width + start[0], watermarkImg.height + start[1])
        false -> listOf(baseImg.width, baseImg.height)
    }
    for (x in start[0] until end[0]) {
        for (y in start[1] until end[1]) {
            val i = Color(baseImg.getRGB(x, y), alphaChannel)
            val w = when (single) {
                true -> Color(watermarkImg.getRGB(x - start[0], y - start[1]), alphaChannel)
                false -> Color(watermarkImg.getRGB(x % watermarkImg.width, y % watermarkImg.height), alphaChannel)
            }
            val pixelIsBackground = transparent && w == Color(backGroundColor[0], backGroundColor[1], backGroundColor[2])
            val percent = weight * if (pixelIsBackground) 0
            else if (alphaChannel && w.alpha == 255) 1
            else if (alphaChannel && w.alpha == 0) 0 else 1
            val color = Color(
                (percent * w.red + (100 - percent) * i.red) / 100,
                (percent * w.green + (100 - percent) * i.green) / 100,
                (percent * w.blue + (100 - percent) * i.blue) / 100
            )
            blendedImg.setRGB(x, y, color.rgb)
        }
    }
    return blendedImg
}
fun error(msg: String) {
    println(msg)
    exitProcess(0)
}
