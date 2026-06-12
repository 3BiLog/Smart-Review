//package com.example.smartreview.data.repository.mock
//
//import com.example.smartreview.data.mock.MockCommunityData
//import com.example.smartreview.data.model.ChatMessage
//import com.example.smartreview.data.model.ChatRoom
//import com.example.smartreview.data.repository.CommunityRepository
//
///**
// * Mock implementation backed by [MockCommunityData].
// * Replace with remote/local sources when Retrofit/Room are introduced.
// */
//class MockCommunityRepository : CommunityRepository {
//
//    override fun getRooms(): List<ChatRoom> = MockCommunityData.rooms
//
//    override fun getSuggestedRooms(): List<ChatRoom> = MockCommunityData.suggestedRooms
//
//    override fun getRoomName(roomId: String): String = MockCommunityData.roomNameFor(roomId)
//
//    override fun getMessages(roomId: String): List<ChatMessage> = MockCommunityData.defaultMessages
//}
