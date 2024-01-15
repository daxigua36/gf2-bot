package gfbot.models

case class GachaLogResponse(code: Int, message: String, data: GachaData)

case class GachaData(list: List[GachaRecord], next: String)

case class GachaRecord(pool_id: Int, item: Int, time: Long)