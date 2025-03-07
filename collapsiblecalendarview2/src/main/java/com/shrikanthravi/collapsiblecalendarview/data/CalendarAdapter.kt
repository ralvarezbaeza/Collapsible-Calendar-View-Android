package com.shrikanthravi.collapsiblecalendarview.data

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shrikanthravi.collapsiblecalendarview.R
import java.util.*

class CalendarAdapter(context: Context, cal: Calendar) {
    private var mFirstDayOfWeek = 0
    private val mInflater = LayoutInflater.from(context)

    var calendar: Calendar = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    var mItemList = mutableListOf<Day>()
    private val mViewList = mutableListOf<View>()
    private val eventMap = mutableMapOf<String, MutableList<Event>>()

    val count: Int get() = mItemList.size

    fun getItem(position: Int): Day = mItemList[position]
    fun getView(position: Int): View = mViewList[position]

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun addEvent(event: Event) {
        eventMap.computeIfAbsent("${event.year}-${event.month}-${event.day}") { mutableListOf() }.add(event)
    }

    fun refresh() {
        mItemList.clear()
        mViewList.clear()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val offset = 1 - (firstDayOfWeek - mFirstDayOfWeek)
        val length = ((lastDayOfMonth - offset + 1) / 7.0).toInt() * 7

        for (i in offset until length + offset) {
            val (numYear, numMonth, numDay) = getDateForIndex(i, year, month, lastDayOfMonth)
            val day = Day(numYear, numMonth, numDay)

            val view = mInflater.inflate(R.layout.day_layout, null, false).apply {
                findViewById<TextView>(R.id.txt_day).apply {
                    text = day.day.toString()
                    alpha = if (day.month != month) 0.3f else 1f
                }
                findViewById<ImageView>(R.id.img_event_tag).apply {
                    eventMap["${day.year}-${day.month}-${day.day}"]?.firstOrNull()?.let { event ->
                        visibility = View.VISIBLE
                        setColorFilter(event.color, PorterDuff.Mode.SRC_ATOP)
                    } ?: run { visibility = View.GONE }
                }
            }

            mItemList.add(day)
            mViewList.add(view)
        }
    }

    private fun getDateForIndex(index: Int, year: Int, month: Int, lastDayOfMonth: Int): Triple<Int, Int, Int> {
        return when {
            index <= 0 -> {
                val prevMonth = if (month == 0) 11 else month - 1
                val prevYear = if (month == 0) year - 1 else year
                val prevMonthDays = Calendar.getInstance().apply { set(prevYear, prevMonth, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
                Triple(prevYear, prevMonth, prevMonthDays + index)
            }
            index > lastDayOfMonth -> {
                val nextMonth = if (month == 11) 0 else month + 1
                val nextYear = if (month == 11) year + 1 else year
                Triple(nextYear, nextMonth, index - lastDayOfMonth)
            }
            else -> Triple(year, month, index)
        }
    }
}
