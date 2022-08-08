package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Input the image filename:")
    val imgFileName = readln()
    if (File(imgFileName).exists()){
        showFileInfo(imgFileName)
    } else println("The file $imgFileName doesn't exist.")
}
fun showFileInfo(imgFileName: String) {
    val img: BufferedImage = ImageIO.read(File(imgFileName))
    val transparency = when(img.transparency) { 1 -> "OPAQUE"; 2 -> "BITMASK"; 3 -> "TRANSLUCENT"; else -> ""}
    println("Image file: $imgFileName\n" +
            "Width: ${img.width}\n" +
            "Height: ${img.height}\n" +
            "Number of components: ${img.colorModel.numComponents}\n" +
            "Number of color components: ${img.colorModel.numColorComponents}\n" +
            "Bits per pixel: ${img.colorModel.pixelSize}\n" +
            "Transparency: $transparency")
}