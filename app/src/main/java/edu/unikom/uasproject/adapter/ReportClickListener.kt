package edu.unikom.uasproject.adapter

import edu.unikom.uasproject.model.ReportItem

interface ReportClickListener {
    fun onClick(report: ReportItem)
}