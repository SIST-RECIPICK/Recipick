package com.sist.web.restcontroller; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.service.ChatService;
import com.sist.web.service.RecipeDetailService;
import com.sist.web.vo.ChatVO;
import com.sist.web.vo.RecipeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(
originPatterns = "*",
allowCredentials = "true"
)
public class ChatRestController {
	private final ChatService service;
	private final RecipeDetailService rService;
	private final SimpMessagingTemplate template;

    @MessageMapping("/chat-send")
    public void getMessage(ChatVO message) {
 
        System.out.println("roomId: "+ message.getRoom_id());
    	System.out.println("userId: "+ message.getUser_id());
    	System.out.println("message: "+ message.getMessage());
 	
    	service.chatMessageInsert(message);	
    	
    	template.convertAndSend(
            "/sub/chat/room/" + message.getRoom_id(),
            message
        );
    }
    
    @GetMapping("/chat/create")
    public ResponseEntity<Map> chat_create(
    	@RequestParam("user_id1") int user_id1,
    	@RequestParam("user_id2") int user_id2,
    	@RequestParam("recipe_id") int recipe_id
    )
    {
    	System.out.println(user_id1);
    	System.out.println(user_id2);
    	Map map = new HashMap();
    	ChatVO vo = new ChatVO();
    	
    	try {
    		int exist = service.chatRoomExist(user_id1, user_id2);
    		
    		if(exist < 1)
    		{
    			RecipeVO recipeData = rService.recipeDetailData(recipe_id);
    			
    			System.out.println("생성");
    			service.chatRoomCreate(user_id1, user_id2);
    			vo = service.chatRoomData(user_id1, user_id2);
    	
    			vo.setMessage(recipeData.getNickname()+"님 안녕하세요 ["
    					+ recipeData.getRcp_nm()+"] 레시피 재료 문의 드립니다");
    			vo.setUser_id(user_id1);
    			service.chatMessageInsert(vo);	
    		}else
    		{
    			vo = service.chatRoomData(user_id1, user_id2);
    		}
    		 		
    		map.put("vo", vo);
    		
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
    }
    
    @GetMapping("/chat/room_list")
    public ResponseEntity<Map> chat_room_list(@RequestParam("user_id") int user_id)
    {
    	Map map = new HashMap();
 	
    	try {
    		
    		List<ChatVO> roomList = service.chatRoomList(user_id);
    		
 
    		map.put("roomList", roomList);
    		
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
    }
    
    @GetMapping("/chat/message_list")
    public ResponseEntity<Map> chat_message_list(@RequestParam("room_id") int room_id)
    {
    	Map map = new HashMap();
 	
    	try {
    		
    		List<ChatVO> messageList = service.chatMessageList(room_id);
    		
 
    		map.put("messageList", messageList);
    		
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.ok(map);
    }
}