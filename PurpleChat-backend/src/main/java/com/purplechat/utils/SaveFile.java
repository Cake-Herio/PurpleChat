package com.purplechat.utils;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.enums.DateTimePatternEnum;
import com.purplechat.redis.RedisComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


@Component("saveFile")
//TODO 后面对这个类改造一下
public class SaveFile {


	private static final Logger logger = LoggerFactory.getLogger(SaveFile.class);


	@Resource
	private RedisComponent redisComponent;

    public static void saveAvatarFile(MultipartFile avatarFile, MultipartFile avatarCover, AppConfig appConfig, String fileId) throws IOException {
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
		File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);

		if(!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		if (avatarCover == null || avatarCover.isEmpty()) {
			logger.error("avatarCover is null");
		}
		if (avatarFile == null || avatarFile.isEmpty()) {
			logger.error("avatarFile is null");
		}

		String filePath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileId ) + Constants.IMAGE_SUFFIX;
		String coverPath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileId ) + Constants.COVER_IMAGE_SUFFIX ;
		logger.info(filePath);
		logger.info(coverPath);



		saveFile(avatarFile, filePath);
		saveFile(avatarCover, coverPath);






    }



	//存软件
	public static void saveProjectFile(MultipartFile file, AppConfig appConfig, Integer fileId) throws IOException {
		File targetFileFolder = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER);
		if(!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		file.transferTo(new File(targetFileFolder.getAbsoluteFile() + "/" + fileId + Constants.APP_EXE_SUFFIX));
	}

	public static void saveMultipartFile(MultipartFile file,MultipartFile cover, String fileName,  AppConfig appConfig, String fileSuffix) throws IOException {
		//有些文件可能不存在cover 需要判断
		//根据月份分目录
		String month = DateUtils.format(new Date((new Date().getTime())), DateTimePatternEnum.YYYYMM.getPattern());

		File targetFileFolder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
		if(!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		String filePath = null;
		if(ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix)){
			filePath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileName) + Constants.IMAGE_SUFFIX;
		}
		else if(ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix)){
			filePath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileName) + Constants.VIDEO_SUFFIX;
		}else{
			filePath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileName) + fileSuffix;
		}


		String coverPath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + fileName) + Constants.COVER_IMAGE_SUFFIX;
		logger.info("save file to " + filePath);
		file.transferTo(new File(filePath));
		if(cover != null){
			logger.info("save cover to " + coverPath);
			cover.transferTo(new File(coverPath));
		}
	}

	private static void saveFile(MultipartFile multipartFile, String targetPath) throws IOException {
		File targetFile = new File(targetPath);

		try (InputStream inputStream = multipartFile.getInputStream();
			 FileOutputStream outputStream = new FileOutputStream(targetFile)) {

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			logger.error("Failed to save file: " + targetPath, e);
			throw e;
		}
	}
}
