package com.shareshelf.backend.event;

import org.springframework.context.ApplicationEvent;

import com.shareshelf.backend.entity.BorrowRequest;

import lombok.Getter;

@Getter
public class BorrowApprovedEvent extends ApplicationEvent  {
	private final BorrowRequest borrowRequest;

    public BorrowApprovedEvent(Object source, BorrowRequest borrowRequest) {
        super(source);
        this.borrowRequest = borrowRequest;
    }

}
