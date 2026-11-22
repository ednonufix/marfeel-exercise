package com.marfeel.exercise.domain.model;

public sealed interface ProductRef permits S1Product,
                                           S2Product {
}
