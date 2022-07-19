package org.grails.demo

import java.time.LocalDate
import java.time.ZonedDateTime

class Post {
    String title
    LocalDate day
    ZonedDateTime createdDate

    static constraints = {
    }

    static mapWith = "mongo"
}
