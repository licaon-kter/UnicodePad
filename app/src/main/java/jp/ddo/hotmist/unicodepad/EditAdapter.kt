/*
   Copyright 2018 Ryosuke839

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package jp.ddo.hotmist.unicodepad

import android.app.Activity
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import com.mobeta.android.dslv.DragSortListView.DropListener
import com.mobeta.android.dslv.DragSortListView.RemoveListener
import java.util.*

internal class EditAdapter(activity: Activity, pref: SharedPreferences, db: NameDatabase, single: Boolean, private val edit: EditText?) : UnicodeAdapter(activity, db, single), TextWatcher, DropListener, RemoveListener {
    private val list: ArrayList<Int> = ArrayList()
    private var suspend = false
    override fun instantiate(grd: AbsListView?): View? {
        list.clear()
        val str = edit!!.editableText.toString()
        var i = 0
        while (i < str.length) {
            val code = str.codePointAt(i)
            if (code > 0xFFFF) ++i
            list.add(code)
            ++i
        }
        edit.addTextChangedListener(this)
        return super.instantiate(grd)
    }

    override fun destroy() {
        super.destroy()
        edit!!.removeTextChangedListener(this)
    }

    override fun name(): Int {
        return R.string.edit
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItemId(arg0: Int): Long {
        return list[arg0].toLong()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (suspend) return
        if (before == 0 && count == 0) return
        val str = s.toString()
        list.clear()
        var i = 0
        while (i < str.length) {
            val code = str.codePointAt(i)
            if (code > 0xFFFF) ++i
            list.add(code)
            ++i
        }
        view!!.invalidateViews()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {}
    override fun drop(from: Int, to: Int) {
        runOnUiThread {
            suspend = true
            var fromBegin = 0
            var fromEnd = 0
            for (i in list.indices) {
                if (i == from) fromBegin = fromEnd
                if (list[i] > 0xFFFF) ++fromEnd
                ++fromEnd
                if (i == from) break
            }
            edit!!.editableText.delete(fromBegin, fromEnd)
            val ch = list.removeAt(from)
            var toBegin = 0
            for (i in list.indices) {
                if (i == to) break
                if (list[i] > 0xFFFF) ++toBegin
                ++toBegin
            }
            edit.editableText.insert(toBegin, String(Character.toChars(ch)))
            edit.editableText.replace(0, edit.editableText.length, edit.editableText)
            suspend = false
            list.add(to, ch)
            if (view != null) view!!.invalidateViews()
        }
    }

    override fun remove(which: Int) {
        runOnUiThread {
            suspend = true
            var whichBegin = 0
            var whichEnd = 0
            for (i in list.indices) {
                if (i == which) whichBegin = whichEnd
                if (list[i] > 0xFFFF) ++whichEnd
                ++whichEnd
                if (i == which) break
            }
            edit!!.editableText.delete(whichBegin, whichEnd)
            edit.editableText.replace(0, edit.editableText.length, edit.editableText)
            suspend = false
            list.removeAt(which)
            if (view != null) view!!.invalidateViews()
        }
    }

}