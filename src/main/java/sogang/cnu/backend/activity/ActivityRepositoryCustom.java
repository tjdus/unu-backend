package sogang.cnu.backend.activity;

import sogang.cnu.backend.activity.dto.ActivitySearchQuery;

import java.util.List;


public interface ActivityRepositoryCustom {
    List<Activity> search(ActivitySearchQuery query);
}
