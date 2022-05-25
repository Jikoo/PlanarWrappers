package com.github.jikoo.planarwrappers.service;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/** A bridge for Vault-supporting economy plugins. */
public class VaultEconomy extends ManagerProvidedService<Economy> {

  public VaultEconomy(@NotNull Plugin plugin) {
    super(plugin);
  }

  @Override
  protected boolean isUsable(@NotNull Economy provider) {
    return provider.isEnabled();
  }

  @Override
  protected @Nullable Supplier<@NotNull String> logServiceClassNotLoaded() {
    return () -> "[VaultEconomyProvider] Vault is not loaded, cannot use economy integration.";
  }

  @Override
  protected @Nullable Supplier<@NotNull String> logNoProviderRegistered(
      @NotNull Class<Economy> clazz) {
    return () -> "[VaultEconomyProvider] No economy providers are registered.";
  }

  @Override
  protected @Nullable Supplier<String> logServiceProviderChange(
      @NotNull Class<Economy> clazz,
      @NotNull Economy instance) {
    return () -> "[VaultEconomyProvider] Hooked into economy provider " + instance.getName();
  }

  /**
   * Get the name of the active {@link Economy} implementation. If no implementation is present,
   * returns {@code "null"}.
   *
   * @return the name
   */
  public @NotNull String getName() {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return "null";
    }

    return service.unwrap().getName();
  }

  /**
   * Get the number of decimal places kept by the {@link Economy} implementation. Some plugins only
   * support certain levels of precision. If no rounding is done or no implementation is present,
   * returns {@code -1}.
   *
   * @return the number of decimal places kept
   */
  public int getFractionalDigits() {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return -1;
    }

    return service.unwrap().fractionalDigits();
  }

  /**
   * Format an amount into a human-readable String. This allows economies to handle formatting and
   * currency symbol placement.
   *
   * @param amount to format
   * @return a readable description of the amount
   */
  public @NotNull String format(double amount) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return String.valueOf(amount);
    }

    return service.unwrap().format(amount);
  }

  /**
   * Get the name of the currency of the {@link Economy} implementation in plural form. If the
   * implementation does not support currency names or no implementation is present, the return
   * value is empty.
   *
   * @return the plural name of the currency
   */
  public @NotNull String getCurrencyNamePlural() {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return "";
    }

    return service.unwrap().currencyNamePlural();
  }

  /**
   * Get the name of the currency of the {@link Economy} implementation in singular form. If the
   * implementation does not support currency names or no implementation is present, the return
   * value is empty.
   *
   * @return the singular name of the currency
   */
  public @NotNull String getCurrencyNameSingular() {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return "";
    }

    return service.unwrap().currencyNameSingular();
  }

  /**
   * Check if the {@link Economy} implementation has a user account for an {@link OfflinePlayer} in
   * the given world. If no implementation is present, returns {@code false}.
   *
   * @param player the user
   * @param world the world name
   * @return true if the user has an account
   */
  public boolean hasPlayerAccount(@NotNull OfflinePlayer player, @Nullable String world) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return false;
    }

    if (world == null) {
      return service.unwrap().hasAccount(player);
    }

    return service.unwrap().hasAccount(player, world);
  }

  /**
   * Create a user account for an {@link OfflinePlayer} in the specified world. If the
   * implementation does not support account creation or no implementation is present, returns
   * {@code false}.
   *
   * @param player the user
   * @param world the world name
   * @return true if the account was created
   */
  public boolean createPlayerAccount(@NotNull OfflinePlayer player, @Nullable String world) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return false;
    }

    if (world == null) {
      return service.unwrap().createPlayerAccount(player);
    }

    return service.unwrap().createPlayerAccount(player, world);
  }

  /**
   * Get the balance of a user in a world. Balance may be global and not per-world. If no
   * implementation is present, returns {@code 0}.
   *
   * @param player the user
   * @param world the world name
   * @return the amount in the user's account
   */
  public double getPlayerBalance(@NotNull OfflinePlayer player, @Nullable String world) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return 0;
    }

    if (world == null) {
      return service.unwrap().getBalance(player);
    }

    return service.unwrap().getBalance(player, world);
  }

  /**
   * Check if a user's account for the world contains an amount. Balance may be global and not
   * per-world. If no implementation is present, returns {@code 0}.
   *
   * @param player the user
   * @param world the world name
   * @param amount the amount of currency required
   * @return true if the user's account contains the amount specified
   */
  public boolean hasPlayerBalance(
      @NotNull OfflinePlayer player,
      @Nullable String world,
      double amount) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return false;
    }

    if (amount <= 0) {
      return true;
    }

    if (world == null) {
      return service.unwrap().has(player, amount);
    }

    return service.unwrap().has(player, world, amount);
  }

  /**
   * Withdraw or deposit an amount to a user's account. Balance may be global and not per-world.
   * If the transaction fails or no implementation is present, returns {@code false}.
   *
   * @param player the user
   * @param world the world name
   * @param amount the amount to modify the account by
   * @return true if the transaction succeeded
   */
  public boolean modifyPlayerBalance(
      @NotNull OfflinePlayer player,
      @Nullable String world,
      double amount) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return false;
    }

    if (amount == 0) {
      return true;
    }

    Economy economy = service.unwrap();
    Supplier<EconomyResponse> supplier;
    if (amount < 0) {
      // Negative amount is a withdrawal of the inverse.
      if (world == null) {
        supplier = () -> economy.withdrawPlayer(player, -amount);
      } else {
        supplier = () -> economy.withdrawPlayer(player, world, -amount);
      }
    } else {
      if (world == null) {
        supplier = () -> economy.depositPlayer(player, amount);
      } else {
        supplier = () -> economy.depositPlayer(player, world, amount);
      }
    }

    return supplier.get().transactionSuccess();
  }

  /**
   * Get whether the active {@link Economy} implementation supports banks. If no implementation is
   * present, returns {@code false}.
   *
   * @return true if banks are supported
   */
  public boolean hasBankSupport() {
    Wrapper<Economy> service = getService();

    return service != null && service.unwrap().hasBankSupport();
  }

  /**
   * Create a bank account with the specified name and the user as the owner. If no implementation
   * is present, returns {@code false}.
   *
   * @param name the name of the account
   * @param player the account owner
   * @return true if the bank account was created
   */
  public boolean createBank(String name, OfflinePlayer player) {
    Wrapper<Economy> service = getService();

    return service != null && service.unwrap().createBank(name, player).transactionSuccess();
  }

  /**
   * Delete a bank account with the specified name. If no implementation is present, returns
   * {@code false}.
   *
   * @param name the name of the account
   * @return true if the bank account was deleted
   */
  public boolean deleteBank(String name) {
    Wrapper<Economy> service = getService();

    return service != null && service.unwrap().deleteBank(name).transactionSuccess();
  }

  /**
   * Get the balance of a bank account. If no implementation is present, returns {@code 0}.
   *
   * @param name the name of the account
   * @return the amount in the bank account
   */
  public double getBankBalance(String name) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return 0;
    }

    EconomyResponse response = service.unwrap().bankBalance(name);
    return response.transactionSuccess() ? response.balance : 0;
  }

  /**
   * Check if a bank account contains an amount. If no implementation is present, returns
   * {@code false}.
   *
   * @param name the name of the account
   * @param amount the amount of currency required
   * @return true if the bank account contains the amount specified
   */
  public boolean hasBankBalance(String name, double amount) {
    Wrapper<Economy> service = getService();

    return service != null
        && (amount <= 0 || service.unwrap().bankHas(name, amount).transactionSuccess());
  }

  /**
   * Withdraw or deposit an amount to a bank account. If the transaction fails or no implementation
   * is present, returns {@code false}.
   *
   * @param name the name of the account
   * @param amount the amount to modify the account by
   * @return true if the transaction succeeded
   */
  public boolean modifyBankBalance(String name, double amount) {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return false;
    }

    if (amount == 0) {
      return true;
    }

    Economy economy = service.unwrap();
    BiFunction<String, Double, EconomyResponse> function;
    if (amount < 0) {
      function = economy::bankWithdraw;
      amount = -amount;
    } else {
      function = economy::bankDeposit;
    }

    EconomyResponse response = function.apply(name, amount);
    return response.transactionSuccess();
  }

  /**
   * Check if a user is the owner of a bank account. If no implementation is present, returns
   * {@code false}.
   *
   * @param name the name of the account
   * @param player the potential owner
   * @return true if the user is the owner
   */
  public boolean isBankOwner(String name, OfflinePlayer player) {
    Wrapper<Economy> service = getService();

    return service != null && service.unwrap().isBankOwner(name, player).transactionSuccess();
  }

  /**
   * Check if a user is a member of a bank account. If no implementation is present, returns
   * {@code false}.
   *
   * @param name the name of the account
   * @param player the potential bank member
   * @return true if the user is a bank member
   */
  public boolean isBankMember(String name, OfflinePlayer player) {
    Wrapper<Economy> service = getService();

    return service != null && service.unwrap().isBankMember(name, player).transactionSuccess();
  }

  /**
   * Get all bank names. If no implementation is present, returns an empty collection.
   *
   * @return a collection of all bank names.
   */
  public @NotNull @UnmodifiableView Collection<String> getBanks() {
    Wrapper<Economy> service = getService();

    if (service == null) {
      return Collections.emptyList();
    }

    return service.unwrap().getBanks();
  }

}
