package org.example.Service;

import org.example.DTO.Request.MenuItemRequest;
import org.example.DTO.Response.MenuItemResponse;

import java.util.List;

public interface ServiceMenuItem{
    MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest);
    MenuItemResponse updateMenuItem(MenuItemRequest menuItemRequest);
    void deleteMenuItem(MenuItemRequest menuItemRequest);
    MenuItemResponse getFindByIdMenuItem(int menuItemRequest);
    List<MenuItemResponse> getAllMenuItems();
}
