package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.PlayRecordDAO;
import com.sound.model.Sound;
import com.sound.model.User;
import com.sound.model.User.PlayRecord;

@Service
@Scope("singleton")
public class PlayListService implements
		com.sound.service.sound.itf.PlayListService {

	@Autowired
	PlayRecordDAO playRecordDAO;

	@Override
	public void add(User user, Sound sound) {
		PlayRecord record = playRecordDAO.findOne("sound", sound);

		if (null != record) {
			return;
		}

		record = new PlayRecord();
		record.setSound(sound);
		record.setUser(user);
		record.setStatus("live");

		playRecordDAO.save(record);
	}

	@Override
	public void remove(User user, Sound sound) {
		if (null == user && null == sound) {
			return;
		}
		Map<String, Object> cratiaries = new HashMap<String, Object>();
		if (null != user) {
			cratiaries.put("user", user);
		}
		if (null != sound) {
			cratiaries.put("sound", sound);
		}

		playRecordDAO.deleteByProperties(cratiaries);
	}

	@Override
	public List<Sound> list(User user, Integer pageNum, Integer perPage) {
		Map<String, Object> cratiaries = new HashMap<String, Object>();
		cratiaries.put("user", user);
		cratiaries.put("status", "live");
		List<PlayRecord> records = playRecordDAO.findWithRange(cratiaries, (pageNum - 1) * perPage, perPage, "-createdDate");
		List<Sound> sounds = new ArrayList<Sound>();

		for (PlayRecord record : records) {
			sounds.add(record.getSound());
		}

		return sounds;
	}

	@Override
	public void updateStatus(User user, Sound sound, String status) {
		Map<String, Object> cratiaries = new HashMap<String, Object>();
		
		if (null != user)
		{
			cratiaries.put("user", user);
		}
		cratiaries.put("sound", sound);
		playRecordDAO.updateProperty(cratiaries, "status", status);
	}

}
