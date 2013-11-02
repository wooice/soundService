package com.sound.service.sound.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.FeedbackDAO;
import com.sound.model.Feedback;

@Service
@Scope("singleton")
public class FeedbackService implements com.sound.service.sound.itf.FeedbackService {

  @Autowired
  FeedbackDAO feedbackDAO;
  
  @Override
  public void create(Feedback feedback) {
    feedback.setCreatedDate(new Date());
    feedbackDAO.save(feedback);
  }

  @Override
  public List<Feedback> getFeedbacks(Integer pageNum, Integer perPage) {
    return feedbackDAO.findWithRange(Collections.<String, Object>emptyMap(), (pageNum - 1) * perPage, perPage, "createdDate");
  }

}
