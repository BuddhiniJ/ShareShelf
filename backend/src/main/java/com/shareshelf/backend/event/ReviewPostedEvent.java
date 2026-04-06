package com.shareshelf.backend.event;

import org.springframework.context.ApplicationEvent;

import com.shareshelf.backend.entity.Review;

import lombok.Getter;

@Getter
public class ReviewPostedEvent extends ApplicationEvent {

    private final Review review;

    public ReviewPostedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }
}