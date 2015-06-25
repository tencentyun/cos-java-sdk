import com.qcloud.*;

public class Demo {
	//通过控制台获取AppId,SecretId,SecretKey
	public static final int APP_ID = 1000000;
	public static final String SECRET_ID = "SecretId";
	public static final String SECRET_KEY = "SecretKey";

	public static void main(String[] args) {
		CosCloud cos = new CosCloud(APP_ID, SECRET_ID, SECRET_KEY);
		long start = System.currentTimeMillis();		
		//String result = cos.sliceUploadFileFirstStep("robin_test", "/", "Android Game Programming For Dummies.pdf", "D:\\开发资料\\Android\\Android Game Programming For Dummies.pdf");
		//String result = cos.sliceUploadFileFirstStep("robin_test", "/", "Android Apps with Eclipse .pdf", "D:\\开发资料\\Android\\Android Apps with Eclipse .pdf");
		//String result = cos.sliceUploadFile("robin_test", "/", "cn_office_professional_plus_2013_x86_x64_dvd_1149708.iso", "F:\\software\\office 2013\\cn_office_professional_plus_2013_x86_x64_dvd_1149708.iso", 3 * 1024 * 1024);
		//String result = cos.sliceUploadFile("robin_test", "/", "VS2010旗舰版.简体中文.完美破解.x86.iso", "F:\\software\\VS2010旗舰版.简体中文.完美破解.x86.iso", 3 * 1024 * 1024);		
		//String result = cos.sliceUploadFile("robin_test", "/", "红警II共和国之辉(简体中文版).rar", "F:\\红警II共和国之辉(简体中文版).rar");		
		//String result = cos.updateFile("robin_test", "/", "aa.txt", "test file");
		//String result = cos.deleteFolder("robin_test", "/sdk/");
		//String result = cos.deleteFile("robin_test", "/", "aa.txt");
		//String result = cos.getFileStat("robin_test", "/", "aa.txt");
		//String result = cos.createFolder("robin_test", "/sdk/");
		String result = cos.uploadFile("robin_test", "/", "bb.txt", "D:\\aa.txt");
		//String result = cos.getFolderList("robin_test", "/", 20, "", 0, CosCloud.FolderPattern.Both);
		long end = System.currentTimeMillis();
		System.out.println(result);
		System.out.println("总用时：" + (end - start) + "毫秒");
		System.out.println("The End!");
	}
}
