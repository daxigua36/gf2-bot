package gfbot.models

import mongo4cats.bson.ObjectId

case class User(_id: ObjectId, tgId: String, gfId: String, username: String)

