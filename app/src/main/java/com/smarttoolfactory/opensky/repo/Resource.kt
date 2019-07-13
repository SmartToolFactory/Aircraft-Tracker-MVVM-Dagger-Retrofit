package com.smarttoolfactory.opensky.repo

/**
 * A generic class that contains data and status about loading this data.
 * Enums show current state of fetching data.
 * <br></br>**Loading**: Data fetch is on progress. Progress dialog can be shown at this state to notify users
 * <br></br>**Canceled**: Progress is canceled before getting the result
 * <br></br>**Error**: There occurred some error. Notify users with message
 * <br></br>**Success**: Data is retrieved successfully. Display or use data as required
 */
class Resource<T> private constructor(val status: Status, val data: T?,
                                      val message: String?) {

    enum class Status {
        IDLE, LOADING, SUCCESS, ERROR, CANCELED
    }

    companion object {

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> canceled(msg: String?): Resource<T> {
            return Resource(Status.CANCELED, null, msg)
        }
    }
}