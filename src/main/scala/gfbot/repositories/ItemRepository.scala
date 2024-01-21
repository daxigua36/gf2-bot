package gfbot.repositories

case class Item(enName: String, cnName: String, rarity: Int)
object ItemRepository {
  val banners: Map[Int, String] = Map(
    8001 -> "Weapon Banner B (SSR SPAS)",
    7001 -> "Event Banner B (Sabrina)",
    6001 -> "Event Banner A (Daiyan)",
    5001 -> "Weapon Banner B (SSR AK-Alfa)",
    4001 -> "Novice Banner",
    3001 -> "Event Banner B (Tololo)",
    2001 -> "Weapon Banner A (SSR Type 95)",
    1001 -> "Standard Banner"
  )

  val weapons: Map[Int, Item] = Map(
    11038 -> Item("SSR AK-Alfa", "游星", 5),
    11016 -> Item("SSR Vepr-12", "猎心者", 5),
    10333 -> Item("SSR Mosin Nagant", "斯摩希克", 5),
    11047 -> Item("SSR SPAS", "梅扎露娜", 5),
    11020 -> Item("SSR Pecheneg", "光学幻境", 5),
    11044 -> Item("SSR QBZ-191", "金石奏", 5),
    11053 -> Item("SSR Type 95", "重弦", 5),
    11007 -> Item("Hare", "野兔", 4),
    11023 -> Item("Gloria", "格洛利娅", 4),
    11043 -> Item("QBZ-191", "一九一式", 4),
    11037 -> Item("AK-Alfa", "卡拉什-阿尔法", 4),
    11031 -> Item("Robinson XCR", "罗宾逊先进步枪", 4),
    11040 -> Item("MP7", "黑科赫7", 4),
    11021 -> Item("Vepr-12", "莫洛12", 4),
    11046 -> Item("SPAS", "特殊用途自动型霰弹枪", 4),
    11026 -> Item("Pecheneg", "佩切涅", 4),
    10382 -> Item("Galil", "加利尔轻机枪", 4),
    11014 -> Item("Nemesis", "复仇女神", 4),
    10332 -> Item("Mosin Nagant", "莫辛-纳甘", 4),
    11015 -> Item("Taurus Curve", "金牛座曲线", 4),
    11049 -> Item("Nagant M1895", "纳甘左轮", 4),
    10362 -> Item("Stechkin", "斯捷奇金", 4),
    11052 -> Item("Type 95", "九五式", 4),
    11022 -> Item("Stock Hare", "旧式野兔", 3),
    11010 -> Item("Stock Gloria", "旧式格洛利娅", 3),
    11042 -> Item("Stock QBZ-191", "旧式一九一式", 3),
    11036 -> Item("Stock AK-Alfa", "旧式卡拉什-阿尔法", 3),
    11030 -> Item("Stock Robinson XCR", "旧式罗宾逊先进步枪", 3),
    11039 -> Item("Stock MP7", "旧式黑科赫7", 3),
    10381 -> Item("Stock Galil", "旧式加利尔轻机枪", 3),
    11024 -> Item("Stock Pecheneg", "旧式佩切涅", 3),
    11009 -> Item("Stock Nemesis", "旧式复仇女神", 3),
    10331 -> Item("Stock Mosin Nagant", "旧式莫辛-纳甘", 3),
    11017 -> Item("Stock Vepr-12", "旧式莫洛12", 3),
    11045 -> Item("Stock SPAS", "旧式特殊用途自动型霰弹枪", 3),
    10361 -> Item("Stock Stechkin", "旧式斯捷奇金", 3),
    11048 -> Item("Stock Nagant M1895", "旧式纳甘左轮", 3),
    11008 -> Item("Stock Taurus Curve", "旧式金牛座曲线", 3),
    11051 -> Item("Stock Type 95", "旧式九五式", 3),
  )

  val heroes: Map[Int, Item] = Map(
    1032 -> Item("Daiyan", "黛烟", 5),
    1029 -> Item("Sabrina", "塞布丽娜", 5),
    1025 -> Item("Tololo", "托洛洛", 5),
    1015 -> Item("Vepley", "维普蕾", 5),
    1021 -> Item("Peritya", "佩里提亚", 5),
    1033 -> Item("Mosin Nagant", "莫辛纳甘", 5),
    1027 -> Item("Qiongjiu", "琼玖", 5),
    1017 -> Item("Groza", "闪电", 4),
    1001 -> Item("Charolic", "克罗丽科", 4),
    1008 -> Item("Nemesis", "纳美西丝", 4),
    1009 -> Item("Colphne", "寇尔芙", 4),
    1036 -> Item("Ksenia", "科谢妮亚", 4),
    1024 -> Item("Cheeta", "奇塔", 4),
    1022 -> Item("Sharkry", "夏克里", 4),
    1026 -> Item("Nagant", "纳甘", 4),
    1038 -> Item("Littara", "莉塔拉", 4),
  )

  def getAllItems: Map[Int, Item] = weapons ++ heroes
}
