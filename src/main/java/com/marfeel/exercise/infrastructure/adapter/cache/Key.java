package com.marfeel.exercise.infrastructure.adapter.cache;

import com.marfeel.exercise.domain.model.ProductRef;
import com.marfeel.exercise.domain.vo.UserType;

record Key(UserType user, ProductRef product) {
}
