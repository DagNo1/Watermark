package watermark

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import kotlin.system.exitProcess

fun main() {
    println("Input the image filename:")
    val imgFileName = readln()
    checkCompatibility(imgFileName)
    val img: BufferedImage = ImageIO.read(File(imgFileName))
    println("Input the watermark image filename:")
    val watermarkImgFileName = readln()
    checkCompatibility(watermarkImgFileName,"watermark", img.width, img.height)
    val watermarkImg = ImageIO.read((File(watermarkImgFileName)))
    var condition = "none" //one of the 3
    var backGroundColors = listOf<Int>()
    if (watermarkImg.colorModel.numComponents == 4 && watermarkImg.transparency == 3) {
        println("Do you want to use the watermark's Alpha channel?")
        if (readln() == "yes") condition = "alpha"
    } else {
        println("Do you want to set a transparency color?")
        if (readln() == "yes") {
            condition = "transparent"
            println("Input a transparency color ([Red] [Green] [Blue]):")
            backGroundColors = makeRbg(readln().split(" "))
        }
    }
    println("Input the watermark transparency percentage (Integer 0-100):")
    val percent = readln()
    checkPercentage(percent)
    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()
    checkOutput(outputFileName)
    val blendedImg = blendImg(img, watermarkImg, percent.toInt(), condition , backGroundColors)
    val outPutFile = File(outputFileName)
    outPutFile.createNewFile()
    ImageIO.write(blendedImg, outputFileName.substringAfterLast('.'), outPutFile)
    println("The watermarked image ${outPutFile.path} has been created.")
}
fun checkCompatibility(imgFileName: String, type: String = "image", width: Int = 0, height: Int = 0) {
    if (!File(imgFileName).exists()) error("The file $imgFileName doesn't exist.")
    val img: BufferedImage = ImageIO.read(File(imgFileName))
    if (img.colorModel.numColorComponents != 3) error("The number of $type color components isn't 3.")
    if (img.colorModel.pixelSize != 24 && img.colorModel.pixelSize != 32) error("The $type isn't 24 or 32-bit.")
    if (type == "image") return // THE PART AFTER THIS ONLY CONCERNS WATERMARK IMAGES
    if (img.width != width || img.height != height) error("The image and watermark dimensions are different.")
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
fun checkPercentage(percent : String) {
    try { percent.toInt() } catch (e: Exception) { error("The transparency percentage isn't an integer number.")}
    if (percent.toInt() !in 0..100) error("The transparency percentage is out of range.")
}
fun checkOutput(fileName: String) {
    val format = fileName.substringAfterLast('.')
    if (format != "jpg" && format != "png") error("The output file extension isn't \"jpg\" or \"png\".")
}
fun blendImg(img: BufferedImage, watermarkImg: BufferedImage, weight: Int, condition: String, backGround: List<Int>): BufferedImage {
    val alphaChannel = condition == "alpha"
    val transparent = condition == "transparent"
    val blendedImg = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)
    for (x in 0 until img.width) {
        for (y in 0 until img.height) {
            val i = Color(img.getRGB(x, y),alphaChannel)
            val w = Color(watermarkImg.getRGB(x, y),alphaChannel)
            val pixelIsBackground = transparent && w == Color(backGround[0],backGround[1],backGround[2])
            val percent = weight * if (pixelIsBackground) 0 else if (alphaChannel && w.alpha == 255) 1 else if (alphaChannel && w.alpha == 0) 0 else 1
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
