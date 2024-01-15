package gfbot.models

import canoe.api.models.Keyboard
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}

object MainMenu {
  val cnBtn: KeyboardButton = KeyboardButton.text("中文")
  val enBtn: KeyboardButton = KeyboardButton.text("English")
  val ruBtn: KeyboardButton = KeyboardButton.text("Русский")
  val all: Seq[KeyboardButton] = Seq(ruBtn, cnBtn, enBtn)

  val keyboard: Keyboard.Reply =
    Keyboard.Reply(ReplyKeyboardMarkup(Seq(all), resizeKeyboard = Some(true)))
}
