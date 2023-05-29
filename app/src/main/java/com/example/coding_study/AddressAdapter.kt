package com.example.coding_study

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AddressAdapter(private var addressList: List<Feature>, private var itemClickListener: ItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedItem = -1

    interface ItemClickListener {
        fun onItemClick(fullNm: String)
    }

    fun setSelectedItem(position: Int) { // 선택된 주소 색상 변경을 위한 함수
        selectedItem = position
        notifyDataSetChanged()
    }

    //주소 아이템 사이에 공백, 선 추가해주는 MyItemDecoration 클래스
    class MyItemDecoration(private val dividerHeight: Int, private val margin: Int) : RecyclerView.ItemDecoration() {

        private val dividerPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        // 아이템 사이에 선을 그려주는 함수
        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)

            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            for (i in 0 until parent.childCount - 1) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + layoutParams.bottomMargin + margin // dividerHeight 외에도 margin 값을 더해줍니다.
                val bottom = top + dividerHeight

                c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), top.toFloat(), dividerPaint)
            }
        }

        //아이템 사이에 공백을 주는 함수
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = dividerHeight + margin // dividerHeight 외에도 margin 값을 더해줍니다.
            outRect.left = dividerHeight + margin
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 새로운 View 생성
        val addressTextView = TextView(parent.context)
        return object : RecyclerView.ViewHolder(addressTextView) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 검색 결과를 TextView에 표시

        val addressItem = addressList[position]
        val textView = holder.itemView as TextView
        textView.text = addressItem.properties.full_nm // addressRecyclerView의 아이템에 full_nm 출력

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f) // 아이템(주소) 글씨 크기 변경

        if (selectedItem == position) { // 선택된 아이템의 글씨색 변경
            textView.setTextColor(Color.parseColor("#6185E8"))
        } else { // 선택되지 않은 아이템의 글씨색 설정
            textView.setTextColor(Color.parseColor("#515151"))
        }

        // 클릭 이벤트 설정
        textView.setOnClickListener {
            itemClickListener.onItemClick(addressItem.properties.full_nm)
            setSelectedItem(position) // 선택된 주소 text 색상 변경
        }
    }

    override fun getItemCount() = addressList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Feature>?) {
        addressList = (newList ?: emptyList())
        notifyDataSetChanged()
    }
}
