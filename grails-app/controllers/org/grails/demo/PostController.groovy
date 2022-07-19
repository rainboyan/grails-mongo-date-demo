package org.grails.demo

import grails.rest.*
import grails.converters.*

class PostController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        respond Post.list()
    }

    def save(Post post) {
        post.save()

        respond post
    }
}
