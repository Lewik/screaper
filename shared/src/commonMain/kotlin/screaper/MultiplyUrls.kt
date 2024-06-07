package screaper

fun copyWithMultipliedUrls(multiplier: Int, url: String): List<String> = (1..multiplier).map {
      url.replace("(i)", it.toString())
  }