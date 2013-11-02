package com.sound.service.sound.itf;

import java.util.List;

import com.sound.model.Feedback;

public interface FeedbackService {

  public void create(Feedback feedback);
  
  public List<Feedback> getFeedbacks(Integer pageNum, Integer perPage);
  
}
