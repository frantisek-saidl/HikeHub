## Hardcoded text
- místo hardcoded textu je nutno použít předdělané textové bloky v souboru strings.xml -> /res/values/strings.xml
- takže například místo **`android:text="Submit"`** použít **`android:text="@strings/submit"`**
- submit string v strings.xml: **`<string name="submit">Submit</string>`**
## Naming convention
- psal bych všechno co nejlíp sjednoceně -> <a href="https://cs.wikipedia.org/wiki/CamelCase">camelCase</a> (první písmeno malé, další slova začínají velkým písmenem)
- nejdřív typ (button, textView, label...) pak název (Username, Password): **`android:id="@id/labelUsername"`**
