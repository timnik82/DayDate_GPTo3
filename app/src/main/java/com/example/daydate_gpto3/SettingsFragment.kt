class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var fontStyleSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        textSizeSeekBar = binding.seekBarTextSize
        fontStyleSpinner = binding.spinnerFontStyle

        // Load saved text size
        val savedTextSize = sharedPreferences.getFloat("text_size", 1.0f)
        textSizeSeekBar.progress = (savedTextSize * 100).toInt()

        // Set up text size seekbar listener
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val textSize = progress / 100f
                sharedPreferences.edit().putFloat("text_size", textSize).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set up font style spinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.font_styles,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        fontStyleSpinner.adapter = adapter

        // Load saved font style
        val savedFontStyle = sharedPreferences.getString("font_style", "Default")
        val position = adapter.getPosition(savedFontStyle)
        fontStyleSpinner.setSelection(position)

        // Set up font style spinner listener
        fontStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFontStyle = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("font_style", selectedFontStyle).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
} 