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
    var calendar: Calendar
    private val mInflater: LayoutInflater
    val mItemList = ArrayList<Day>()
    private val mViewList = ArrayList<View>()
    var mEventList = ArrayList<Event>()

    // Usamos un Map mutable para agregar eventos a las fechas
    private val eventMap = mutableMapOf<String, MutableList<Event>>()

    val count: Int
        get() = mItemList.size

    init {
        this.calendar = cal.clone() as Calendar
        this.calendar.set(Calendar.DAY_OF_MONTH, 1)
        mInflater = LayoutInflater.from(context)
        refresh()
    }

    fun getItem(position: Int): Day = mItemList[position]

    fun getView(position: Int): View = mViewList[position]

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun addEvent(event: Event) {
        mEventList.add(event)
        val eventKey = "${event.year}-${event.month}-${event.day}"

        // Usamos computeIfAbsent para inicializar la lista de eventos si no existe
        eventMap.computeIfAbsent(eventKey) { mutableListOf() }.add(event)
    }

    fun refresh() {
        // Limpiar datos antiguos
        mItemList.clear()
        mViewList.clear()

        // Obtener año, mes y primer día de la semana
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendar.set(year, month, 1)

        // Ajustar el offset
        val offset = 0 - (firstDayOfWeek - mFirstDayOfWeek) + 1
        val length = Math.ceil(((lastDayOfMonth - offset + 1).toFloat() / 7).toDouble()).toInt() * 7

        for (i in offset until length + offset) {
            val (numYear, numMonth, numDay) = getDateForIndex(i, year, month, lastDayOfMonth)

            val day = Day(numYear, numMonth, numDay)
            val view = mInflater.inflate(R.layout.day_layout, null)
            val txtDay = view.findViewById<TextView>(R.id.txt_day)
            val imgEventTag = view.findViewById<ImageView>(R.id.img_event_tag)

            txtDay.text = day.day.toString()
            txtDay.alpha = if (day.month != calendar.get(Calendar.MONTH)) 0.3f else 1f

            // Usamos el Map para obtener los eventos directamente sin recorrer toda la lista
            val eventKey = "${day.year}-${day.month}-${day.day}"
            eventMap[eventKey]?.forEach { event ->
                imgEventTag.visibility = View.VISIBLE
                imgEventTag.setColorFilter(event.color, PorterDuff.Mode.SRC_ATOP)
            }

            mItemList.add(day)
            mViewList.add(view)
        }
    }

    private fun getDateForIndex(index: Int, year: Int, month: Int, lastDayOfMonth: Int): Triple<Int, Int, Int> {
        var numYear = year
        var numMonth = month
        var numDay = index

        val tempCal = Calendar.getInstance()

        when {
            index <= 0 -> {
                if (month == 0) {
                    numYear = year - 1
                    numMonth = 11
                } else {
                    numYear = year
                    numMonth = month - 1
                }
                tempCal.set(numYear, numMonth, 1)
                numDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) + index
            }
            index > lastDayOfMonth -> {
                if (month == 11) {
                    numYear = year + 1
                    numMonth = 0
                } else {
                    numYear = year
                    numMonth = month + 1
                }
                tempCal.set(numYear, numMonth, 1)
                numDay = index - lastDayOfMonth
            }
        }

        return Triple(numYear, numMonth, numDay)
    }
}