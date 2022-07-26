package com.example.cryptomarket.ui.coins

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cryptomarket.R
import com.example.cryptomarket.data.coinsapi.ticker.HistoricalTicker
import com.example.cryptomarket.data.coinsapi.ticker.PriceData
import com.example.cryptomarket.data.coinsapi.ticker.Ticker
import com.example.cryptomarket.databinding.FragmentCoinsListBinding
import com.example.cryptomarket.ui.CryptoViewModel
import com.example.cryptomarket.utils.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val TAG = "CoinsListFrag__TAG"

class CoinsListFragment : Fragment() {

    private var binding: FragmentCoinsListBinding? = null
    private val vm: CryptoViewModel by activityViewModels()
    private lateinit var coinsListAdapter: CoinsListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var linearMarker: LinearMarker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentCoinsListBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearMarker = LinearMarker(requireContext(), R.layout.line_chart_marker)
        coinsListAdapter =
            CoinsListAdapter(viewLifecycleOwner, DateFrame.WEEK, resources, linearMarker, vm)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            coinsListRecycler.adapter = coinsListAdapter
            coinsListRecycler.layoutManager = LinearLayoutManager(requireContext())
            minimizeBtn.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            expandedSheetMainContainer.setOnClickListener {
                // The purpose of this click listener is to cover
                //  up a bug clicking list items behind the sheet.
            }
        }
        setObservers()
        setBottomSheetBehavior()
    }

    override fun onResume() {
        super.onResume()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    // SETUP //
    private fun setBottomSheetBehavior() {
        binding?.apply {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
            bottomSheetBehavior.peekHeight =
                230  // Should be the same height as collapsedDataContainer
            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                @SuppressLint("SwitchIntDef")
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            toggleAppbar(false)
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            toggleAppbar(true)
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            Log.i(TAG, "onStateChanged: STATE_DRAGGING")
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                            if (collapsedDataContainer.visibility == View.VISIBLE)
                                collapsedDataContainer.visibility = View.INVISIBLE
                            if (appBarLayout.visibility == View.VISIBLE)
                                appBarLayout.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            }
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
            collapsedDataContainer.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun setObservers() {
        vm.fragChosen.observe(viewLifecycleOwner) {
            val navController = findNavController()
            when (it) {
                FragChosen.MARKET -> navController.navigate(R.id.action_coins_to_market)
                FragChosen.NEWS -> navController.navigate(R.id.action_coins_to_news)
                else -> Log.i(TAG, "setObservers: from Coins to $it")
            }
        }
        vm.tickerClicked.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            setCoinOnSheet(it)
            timeFrameClickListeners(it.id)
        }
        vm.tickers.observe(viewLifecycleOwner) {
            coinsListAdapter.submitList(it)
            setCoinOnSheet(it[0])
            timeFrameClickListeners(it[0].id)
            populateCharts(vm.getHistoricalTickerData(true, it[0].id, DateFrame.WEEK))
        }
    }
    // SETUP //

    // HELPERS
    private fun setCoinOnSheet(ticker: Ticker) {
        binding?.apply {
            tickerSymbolTxt.text = ticker.symbol
            collapsedSymbolTxt.text = ticker.symbol
            totalSupplyTxt.text = displayLong(ticker.totalSupply)

            percentChange7dTxt.text = displayPercent("1w: ", ticker.quotes.usd.percentChange7d)

            tickerPriceTxt.text = presentPriceFormatUSD("", ticker.quotes.usd.price)
            percentChangeATxt.text = displayPercent("1w: ", ticker.quotes.usd.percentChange7d)
            percentChangeBTxt.text = displayPercent("1m: ", ticker.quotes.usd.percentChange30d)
            setMoreInfoDataToUI(ticker.quotes.usd, ticker.id)
        }
    }

    private fun timeFrameClickListeners(tickerID: String) {
        binding?.apply {
            wBtn.isChecked = true
            timeframeBtnGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        wBtn.id -> populateCharts(
                            vm.getHistoricalTickerData(true, tickerID, DateFrame.WEEK)
                        )
                        mBtn.id -> populateCharts(
                            vm.getHistoricalTickerData(true, tickerID, DateFrame.MONTH)
                        )
                        qBtn.id -> populateCharts(
                            vm.getHistoricalTickerData(true, tickerID, DateFrame.QUARTER)
                        )
                        sixMBtn.id -> populateCharts(
                            vm.getHistoricalTickerData(true, tickerID, DateFrame.HALF_YEAR)
                        )
                        yBtn.id -> populateCharts(
                            vm.getHistoricalTickerData(true, tickerID, DateFrame.YEAR)
                        )
                    }
                }
            }
        }
    }

    private fun setMoreInfoDataToUI(priceData: PriceData, coinID: String) {
        val notAvailable = "not available"
        vm.getCoinData(coinID).observe(viewLifecycleOwner) {
            binding?.apply {
                rankTxt.text = it.rank.toString()
                coinName2Txt.text = it.name
                typeTxt.text = it.type?.replaceFirstChar { it.titlecase() } ?: "Type $notAvailable"
                teamTxt.text = displayTeam(it.team)
                descriptionTxt.text = it.description ?: "Description $notAvailable"
                openSourceTxt.text = displayIsOpenSource(it.openSource)
                startedAtTxt.text = displayMoreInfo("Started at", reformatDate(it.startedAt))
                proofTypeTxt.text = it.proofType ?: "Proof type $notAvailable"
                orgStructureTxt.text = it.orgStructure ?: "Org structure $notAvailable"
                hashAlgorithmTxt.text = displayHashAlgorithm(it.hashAlgorithm)
                athPriceTxt.text = presentPriceFormatUSD("- ", priceData.athPrice)
                athDateTxt.text = displayMoreInfo("-", reformatDate(priceData.athDate))
            }
        }
    }

    private fun populateCharts(tickerData: LiveData<List<HistoricalTicker>>) {
        binding?.apply {
            tickerData.observe(viewLifecycleOwner) {
                makeCollapsedChart(tickerChartCollapsed, it)
                makeExpandedChart(tickerChartExpanded, it)
            }
        }
    }

    private fun makeCollapsedChart(chart: LineChart, tickerData: List<HistoricalTicker>) {
        // todo
        chart.setBackgroundColor(resources.getColor(R.color.white))
//            chart.setTouchEnabled(true)

        chart.setDrawGridBackground(false)

        // create marker to display box when values are selected
        // Set the marker to the chart
        linearMarker.chartView = chart
        chart.marker = linearMarker

        // enable scaling and dragging
//            chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
//            chart.setPinchZoom(true)

        // add data
        setCollapsedChartData(chart, tickerData)
        // draw points over time
        chart.animateX(1500)
        // get the legend (only possible after setting data)
        val l: Legend = chart.legend
        // draw legend entries as lines
        l.form = Legend.LegendForm.LINE
    }

    private fun makeExpandedChart(chart: LineChart, tickerData: List<HistoricalTicker>) {
        // todo
        chart.setBackgroundColor(resources.getColor(R.color.white))
//            chart.setTouchEnabled(true)

        chart.setDrawGridBackground(false)

        // create marker to display box when values are selected
        // Set the marker to the chart
        linearMarker.chartView = chart
        chart.marker = linearMarker

        // enable scaling and dragging
//            chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
//            chart.setPinchZoom(true)

        // add data
        setExpandedChartData(chart, tickerData)
        // draw points over time
        chart.animateX(1500)
        // get the legend (only possible after setting data)
        val l: Legend = chart.legend
        // draw legend entries as lines
        l.form = Legend.LegendForm.LINE
    }

    private fun setCollapsedChartData(chart: LineChart, tickerData: List<HistoricalTicker>) {
        // todo
        val set1: LineDataSet?
        val entries = ArrayList<Entry>()

        for (i in tickerData.indices) {
            entries.add(
                Entry(i.toFloat(), tickerData[i].price.toFloat())
            )
        }

        if (chart.data != null && chart.data.dataSetCount > 0) {
            // If data has already been created.
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(entries, "DataSet 1")
            set1.setDrawIcons(false)
            // draw dashed line
//                set1.enableDashedLine(10f, 5f, 0f)
            // black lines and points
            set1.color = Color.BLACK        // todo: change the color of the line
            // line thickness and point size
            set1.lineWidth = 1f     // todo: change the width of the line

//                set1.setCircleColor(Color.BLACK)
//                set1.circleRadius = 3f
//                set1.setDrawCircleHole(false)
            set1.setDrawCircles(false)

            // customize legend entry
            // todo: take out legend
//                set1.formLineWidth = 1f
//                set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//                set1.formSize = 15f

            // todo: get rid of this text (for the value of the data in the line)
            // text size of values
            set1.valueTextSize = 9f

            // todo: take out the grids using this (probably)
//                xAxis = lineChart.xAxis
////        xAxis.enableGridDashedLine(10f, 10f, 0f)
//                yAxis = lineChart.axisLeft
////        // disable dual axis (only use LEFT axis)
//                lineChart.axisRight.isEnabled = false
////        // horizontal grid lines
////        yAxis.enableGridDashedLine(10f, 10f, 0f)
////        // axis range
//                yAxis.axisMaximum = 200f
//                yAxis.axisMinimum = -50f
//                xAxis.setDrawGridLines(false)
//                yAxis.setDrawGridLines(false)
            chart.axisRight.isEnabled = false



            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets
            // create a data object with the data sets
            val data = LineData(dataSets)
            // set data
            chart.data = data
        }
    }

    private fun setExpandedChartData(chart: LineChart, tickerData: List<HistoricalTicker>) {
        // todo
        val set1: LineDataSet?
        val entries = ArrayList<Entry>()

        for (i in tickerData.indices) {
            entries.add(
                Entry(i.toFloat(), tickerData[i].price.toFloat())
            )
        }

        if (chart.data != null && chart.data.dataSetCount > 0) {
            // If data has already been created.
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = entries
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(entries, "DataSet 1")
            set1.setDrawIcons(false)
            // draw dashed line
//                set1.enableDashedLine(10f, 5f, 0f)
            // black lines and points
            set1.color = Color.BLACK        // todo: change the color of the line
            // line thickness and point size
            set1.lineWidth = 1f     // todo: change the width of the line

//                set1.setCircleColor(Color.BLACK)
//                set1.circleRadius = 3f
//                set1.setDrawCircleHole(false)
            set1.setDrawCircles(false)

            // customize legend entry
            // todo: take out legend
//                set1.formLineWidth = 1f
//                set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//                set1.formSize = 15f

            // todo: get rid of this text (for the value of the data in the line)
            // text size of values
            set1.valueTextSize = 9f

            // todo: take out the grids using this (probably)
//                xAxis = lineChart.xAxis
////        xAxis.enableGridDashedLine(10f, 10f, 0f)
//                yAxis = lineChart.axisLeft
////        // disable dual axis (only use LEFT axis)
//                lineChart.axisRight.isEnabled = false
////        // horizontal grid lines
////        yAxis.enableGridDashedLine(10f, 10f, 0f)
////        // axis range
//                yAxis.axisMaximum = 200f
//                yAxis.axisMinimum = -50f
//                xAxis.setDrawGridLines(false)
//                yAxis.setDrawGridLines(false)
            chart.axisRight.isEnabled = false



            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets
            // create a data object with the data sets
            val data = LineData(dataSets)
            // set data
            chart.data = data
        }
    }

    private fun toggleAppbar(hideAppbar: Boolean) {
        binding!!.apply {
            if (hideAppbar) {
                appBarLayout.visibility = View.GONE
                collapsedDataContainer.visibility = View.VISIBLE
            } else {
                collapsedDataContainer.visibility = View.GONE
                appBarLayout.visibility = View.VISIBLE
            }
        }
    }
    // HELPERS
}