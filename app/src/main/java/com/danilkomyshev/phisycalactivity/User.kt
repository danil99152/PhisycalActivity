package com.danilkomyshev.phisycalactivity

import org.json.JSONException
import org.json.JSONObject

class User(name: String, id: String) {
    var name: String? = name
    var id: String? = id

    override fun toString(): String {
        return "User{" + this.name + ":" + this.id + "}"
    }

    companion object {

        @Throws(JSONException::class)
        fun fromJSON(response: JSONObject): User {
            return User(response.getString("name"), response.getString("id"))
        }
    }
}
