package com.zx.painel

import android.content.Context
import android.graphics.*
import android.view.*
import android.widget.*

class PanelView(context: Context) : LinearLayout(context) {

    var espAtivo = false
    var espLinha = false
    var espCaixa = false
    var espEsqueleto = false
    var espNome = false
    var espVida = false
    var aimbotAtivo = false
    var puxarParedes = false
    var exibirFov = false
    var exibirLinha = false
    var fovValue = 180f
    var ossoPuxada = "Cabeça"
    var tipoAimbot = "Ao Atirar"
    var aimKill = false
    var teleport = false
    var voarPlayer = false
    var speed = false
    var noRecoil = false

    private val config = ConfigManager(context)
    private val inputInjector = InputInjector(context)
    private val aimbotEngine = AimbotEngine(context, inputInjector)

    private val TAB_AIMBOT = 0
    private val TAB_ESP = 1
    private val TAB_CONFIG = 2
    private val TAB_INFO = 3
    private var currentTab = TAB_AIMBOT
    private lateinit var contentArea: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#CC000000"))
        minimumWidth = 320.dp
        buildUI()
        loadConfig()
    }

    private fun buildUI() {
        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setBackgroundColor(Color.parseColor("#CC1a1a1a"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
        }
        val title = TextView(context).apply {
            text = "ZX PAINEL"
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LayoutParams(0, WRAP_CONTENT, 1f)
        }
        val closeBtn = TextView(context).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(8.dp, 0, 0, 0)
            setOnClickListener {
                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                    ?.removeView(this@PanelView)
            }
        }
        header.addView(title)
        header.addView(closeBtn)
        addView(header)

        contentArea = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
        }
        addView(contentArea)

        val tabBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setBackgroundColor(Color.parseColor("#CC111111"))
            setPadding(8.dp, 6.dp, 8.dp, 6.dp)
        }
        listOf("🎯", "👁", "⚙", "ℹ").forEachIndexed { i, icon ->
            val tab = TextView(context).apply {
                text = icon
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(12.dp, 6.dp, 12.dp, 6.dp)
                layoutParams = LayoutParams(0, WRAP_CONTENT, 1f)
                setBackgroundColor(if (i == 0) Color.parseColor("#BB1a0000") else Color.TRANSPARENT)
                setOnClickListener {
                    currentTab = i
                    renderTab()
                    (parent as? LinearLayout)?.let { bar ->
                        for (j in 0 until bar.childCount) {
                            (bar.getChildAt(j) as? TextView)?.setBackgroundColor(
                                if (j == i) Color.parseColor("#BB1a0000") else Color.TRANSPARENT
                            )
                        }
                    }
                }
            }
            tabBar.addView(tab)
        }
        addView(tabBar)
        renderTab()
    }

    private fun renderTab() {
        contentArea.removeAllViews()
        when (currentTab) {
            TAB_AIMBOT -> buildAimbotTab()
            TAB_ESP    -> buildEspTab()
            TAB_CONFIG -> buildConfigTab()
            TAB_INFO   -> buildInfoTab()
        }
    }

    private fun buildAimbotTab() {
        contentArea.addView(rowOf(
            checkItem("Ativar Aimbot", aimbotAtivo) { v -> aimbotAtivo = v; aimbotEngine.enabled = v },
            checkItem("Puxar em Paredes", puxarParedes) { v -> puxarParedes = v }
        ))
        contentArea.addView(rowOf(
            checkItem("Exibir FOV", exibirFov) { v -> exibirFov = v },
            checkItem("Exibir Linha", exibirLinha) { v -> exibirLinha = v }
        ))
        contentArea.addView(sliderItem("Regular FOV", fovValue, 10f, 360f) { v ->
            fovValue = v; aimbotEngine.fov = v
        })
        contentArea.addView(dropdownItem("Puxada", listOf("Cabeça","Pescoço","Peito","Abdômen")) { v ->
            ossoPuxada = v; aimbotEngine.bone = v
        })
        contentArea.addView(TextView(context).apply {
            text = "Tipo de Aimbot:"
            setTextColor(Color.WHITE)
            textSize = 11f
            setPadding(0, 8.dp, 0, 4.dp)
        })
        val tipoRow = LinearLayout(context).apply { orientation = HORIZONTAL }
        listOf("Ao Atirar", "Ao Olhar").forEach { tipo ->
            tipoRow.addView(RadioButton(context).apply {
                text = tipo
                setTextColor(Color.WHITE)
                textSize = 11f
                isChecked = tipoAimbot == tipo
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnClickListener { tipoAimbot = tipo; aimbotEngine.triggerMode = tipo }
            })
        }
        contentArea.addView(tipoRow)
        contentArea.addView(rowOf(
            checkItem("AimKill", aimKill) { v -> aimKill = v },
            checkItem("Teleport 8m", teleport) { v -> teleport = v },
            checkItem("Voar Player", voarPlayer) { v -> voarPlayer = v }
        ))
        contentArea.addView(rowOf(
            redButton("Speed (Beta)") { speed = !speed },
            redButton("No Recoil") { noRecoil = !noRecoil; aimbotEngine.noRecoil = noRecoil }
        ))
    }

    private fun buildEspTab() {
        contentArea.addView(rowOf(
            checkItem("Ativar ESP", espAtivo) { v -> espAtivo = v },
            checkItem("Esconder Painel", false) { v -> visibility = if (v) GONE else VISIBLE }
        ))
        listOf(
            Triple("ESP Linha",     espLinha)     { v: Boolean -> espLinha = v },
            Triple("ESP Caixa",     espCaixa)     { v: Boolean -> espCaixa = v },
            Triple("ESP Esqueleto", espEsqueleto) { v: Boolean -> espEsqueleto = v },
            Triple("ESP Nome",      espNome)      { v: Boolean -> espNome = v },
            Triple("ESP Vida",      espVida)      { v: Boolean -> espVida = v }
        ).forEach { (label, state, cb) ->
            contentArea.addView(checkItem(label, state, cb))
        }
    }

    private fun buildConfigTab() {
        contentArea.addView(redButton("Salvar Config") { saveConfig() })
        contentArea.addView(spacer(4))
        contentArea.addView(redButton("Carregar Config") { loadConfig(); renderTab() })
        contentArea.addView(spacer(4))
        contentArea.addView(redButton("Destruir Hack (BYPASS)") {
            aimbotEngine.shutdown()
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?.removeView(this@PanelView)
        })
    }

    private fun buildInfoTab() {
        contentArea.addView(TextView(context).apply {
            text = "ZX Painel\nVersão 1.0\n\nShizuku: necessário para input\nSem root necessário"
            setTextColor(Color.LTGRAY)
            textSize = 11f
            setPadding(4.dp, 8.dp, 4.dp, 8.dp)
        })
    }

    private fun checkItem(label: String, initial: Boolean, onChange: (Boolean) -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4.dp, 8.dp, 4.dp)
            val cb = CheckBox(context).apply {
                isChecked = initial
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnCheckedChangeListener { _, v -> onChange(v) }
            }
            val tv = TextView(context).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 11f
            }
            addView(cb)
            addView(tv)
        }
    }

    private fun sliderItem(label: String, initial: Float, min: Float, max: Float, onChange: (Float) -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(0, 4.dp, 0, 4.dp)
            val valText = TextView(context).apply {
                text = initial.toInt().toString()
                setTextColor(Color.WHITE)
                textSize = 11f
            }
            val header = LinearLayout(context).apply {
                orientation = HORIZONTAL
                addView(valText.also { it.layoutParams = LayoutParams(0, WRAP_CONTENT, 1f) })
                addView(TextView(context).apply {
                    text = label
                    setTextColor(Color.LTGRAY)
                    textSize = 11f
                })
            }
            val slider = SeekBar(context).apply {
                this.max = (max - min).toInt()
                progress = (initial - min).toInt()
                progressTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                thumbTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar, p: Int, u: Boolean) {
                        val v = p + min; onChange(v); valText.text = v.toInt().toString()
                    }
                    override fun onStartTrackingTouch(sb: SeekBar) {}
                    override fun onStopTrackingTouch(sb: SeekBar) {}
                })
            }
            addView(header)
            addView(slider)
        }
    }

    private fun dropdownItem(label: String, options: List<String>, onChange: (String) -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4.dp, 0, 4.dp)
            val spinner = Spinner(context).apply {
                adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, options)
                layoutParams = LayoutParams(0, WRAP_CONTENT, 1f)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) { onChange(options[pos]) }
                    override fun onNothingSelected(p: AdapterView<*>) {}
                }
            }
            addView(spinner)
            addView(TextView(context).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 11f
                setPadding(8.dp, 0, 0, 0)
            })
        }
    }

    private fun redButton(label: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 11f
            setBackgroundColor(Color.parseColor("#CC880000"))
            setPadding(8.dp, 4.dp, 8.dp, 4.dp)
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                setMargins(0, 2.dp, 4.dp, 2.dp)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun rowOf(vararg views: View): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            views.forEach { addView(it) }
        }
    }

    private fun spacer(dp: Int): View {
        return View(context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, dp.dp)
        }
    }

    private fun saveConfig() {
        config.save(mapOf(
            "espAtivo" to espAtivo, "espLinha" to espLinha, "espCaixa" to espCaixa,
            "espEsqueleto" to espEsqueleto, "espNome" to espNome, "espVida" to espVida,
            "aimbotAtivo" to aimbotAtivo, "puxarParedes" to puxarParedes,
            "exibirFov" to exibirFov, "fovValue" to fovValue,
            "noRecoil" to noRecoil, "ossoPuxada" to ossoPuxada
        ))
    }

    private fun loadConfig() {
        val c = config.load()
        espAtivo     = c["espAtivo"]     as? Boolean ?: false
        espLinha     = c["espLinha"]     as? Boolean ?: false
        espCaixa     = c["espCaixa"]     as? Boolean ?: false
        espEsqueleto = c["espEsqueleto"] as? Boolean ?: false
        espNome      = c["espNome"]      as? Boolean ?: false
        espVida      = c["espVida"]      as? Boolean ?: false
        aimbotAtivo  = c["aimbotAtivo"]  as? Boolean ?: false
        puxarParedes = c["puxarParedes"] as? Boolean ?: false
        exibirFov    = c["exibirFov"]    as? Boolean ?: false
        fovValue     = (c["fovValue"]    as? Double)?.toFloat() ?: 180f
        noRecoil     = c["noRecoil"]     as? Boolean ?: false
        ossoPuxada   = c["ossoPuxada"]   as? String  ?: "Cabeça"
    }

    private val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()
}
