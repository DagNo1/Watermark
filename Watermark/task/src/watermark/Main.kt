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
    if (img.width != baseImg.width || img.height != baseImg.height) error("The image and watermark dimensions are different.")
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
            val i = Color(baseImg.getRGB(x, y),alphaChannel)
            val w = Color(watermarkImg.getRGB(x, y),alphaChannel)
            val pixelIsBackground = transparent && w == Color(backGroundColor[0],backGroundColor[1],backGroundColor[2])
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
