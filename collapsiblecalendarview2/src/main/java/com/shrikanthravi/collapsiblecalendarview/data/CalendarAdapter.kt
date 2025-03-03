package com.shrikanthravi.collapsiblecalendarview.data

import android.content.Context
import android.graphics.PorterDuff
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shrikanthravi.collapsiblecalendarview.R
import java.util.Calendar

class CalendarAdapter(context: Context, cal: Calendar) {
    private var mFirstDayOfWeek = Calendar.SUNDAY
    var calendar: Calendar = cal.clone() as Calendar
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    val mItemList = mutableListOf<Day>()
    private val mViewList = mutableListOf<View>()
    var mEventList = SparseArray<MutableList<Event>>() // Optimiza acceso a eventos por día

    val count: Int
        get() = mItemList.size

    init {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        refresh()
    }

    fun getItem(position: Int): Day = mItemList[position]

    fun getView(position: Int): View = mViewList[position]

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun addEvent(event: Event) {
        val key = event.year * 10000 + event.month * 100 + event.day
        if (mEventList[key] == null) {
            mEventList.put(key, mutableListOf())
        }
        mEventList[key]?.add(event)
    }

    fun refresh() {
        mItemList.clear()
        mViewList.clear()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        calendar.set(year, month, 1)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val offset = -((firstDayOfWeek - mFirstDayOfWeek) + 1)
        val length = ((lastDayOfMonth - offset + 1) / 7.0).toInt() * 7

        for (i in offset until length + offset) {
            val (numYear, numMonth, numDay) = getDayFromOffset(i, year, month, lastDayOfMonth)
            val day = Day(numYear, numMonth, numDay)

            val view = mInflater.inflate(R.layout.day_layout, null)
            val txtDay = view.findViewById<TextView>(R.id.txt_day)
            val imgEventTag = view.findViewById<ImageView>(R.id.img_event_tag)

            txtDay.text = numDay.toString()
            txtDay.alpha = if (numMonth != month) 0.3f else 1.0f

            val key = numYear * 10000 + numMonth * 100 + numDay
            mEventList[key]?.let { events ->
                imgEventTag.visibility = View.VISIBLE
                imgEventTag.setColorFilter(events.first().color, PorterDuff.Mode.SRC_ATOP)
            } ?: run {
                imgEventTag.visibility = View.INVISIBLE
            }

            mItemList.add(day)
            mViewList.add(view)
        }
    }

    private fun getDayFromOffset(
        offset: Int,
        year: Int,
        month: Int,
        lastDay: Int
    ): Triple<Int, Int, Int> {
        val tempCal = Calendar.getInstance()
        return when {
            offset <= 0 -> { // Prev mes
                val prevMonth = if (month == 0) 11 else month - 1
                val prevYear = if (month == 0) year - 1 else year
                tempCal.set(prevYear, prevMonth, 1)
                Triple(
                    prevYear,
                    prevMonth,
                    tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) + offset
                )
            }

            offset > lastDay -> { // Siguiente mes
                val nextMonth = if (month == 11) 0 else month + 1
                val nextYear = if (month == 11) year + 1 else year
                Triple(nextYear, nextMonth, offset - lastDay)
            }

            else -> Triple(year, month, offset)
        }
    }
}
