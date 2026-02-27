package com.yuluo.picture486ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuluo.picture486ddd.domain.picture.entity.Picture;
import com.yuluo.picture486ddd.domain.picture.repository.PictureRepository;
import com.yuluo.picture486ddd.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
